package net.sourceforge.vrapper.vim.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Define a comment text object based on various comment formats.
 * 'ic' - select comment text without comment tokens
 * 'ac' - select comment text including comment tokens
 * 'iC' - select multiple contiguous single comment token lines
 * 'aC' - line-wise select of commented lines
 * 
 * I'm not looking at the file type to see which comment syntax is valid for the
 * file, I'm looking for any comment characters which may exist in the file. To
 * reduce false-positives I'm only looking for lines which start with a
 * single-line comment character.  I'm allowing block comments anywhere in the
 * line simply because they are multi-character tokens and unlikely to be found
 * in non-comment code.  So don't run this command unless you're actually in a comment.
 */
public class CommentTextObject extends AbstractTextObject {
    
    public static final TextObject INNER = new CommentTextObject(false, false);
    public static final TextObject OUTER = new CommentTextObject(true, false);
    public static final TextObject INNER_LINE = new CommentTextObject(false, true);
    public static final TextObject OUTER_LINE = new CommentTextObject(true, true);
    
    //single line comments = //, #, --, ;
    private final Pattern singleLine = Pattern.compile("^\\s*((/(/)+|(#)+|-(-)+|(;)+)\\s*)");
    //block comments = /* */, {- -}, <!-- -->
    private final Pattern blockStart = Pattern.compile("\\s*((/(\\*)+|\\{(-)+|<!-(-)+)\\s*)");
    private final Pattern blockEnd = Pattern.compile("(\\s*((\\*)+/|(-)+\\}|(-)+->))");
    
    private final boolean outer;
    private final boolean linewise;
    
    private CommentTextObject(boolean outer, boolean linewise) {
       this.outer = outer;
       this.linewise = linewise;
    }

    //this method takes a 'count' argument but I'm ignoring it
    //(you can't have nested comments)
    @Override
    public TextRange getRegion(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        TextContent model = editorAdaptor.getModelContent();
        CursorService cursorService = editorAdaptor.getCursorService();

        LineInformation line = model.getLineInformationOfOffset( editorAdaptor.getPosition().getModelOffset() );
        String lineText = model.getText(line.getBeginOffset(), line.getLength());
        Matcher singleLineMatcher = singleLine.matcher(lineText);
        //if this line has a single-line comment, use it
        //otherwise, assume we're inside a block comment
        if(singleLineMatcher.find()) {
            int offset = outer ? singleLineMatcher.start(1) : singleLineMatcher.end(1);
            int start = line.getBeginOffset() + offset;

            if(linewise) { //see if there are any adjacent single-line comments to include
                return getSingleLineComments(outer, start, editorAdaptor);
            }
            else { //we've already found a comment token, use it
                return new StartEndTextRange(
                        cursorService.newPositionForModelOffset(start),
                        cursorService.newPositionForModelOffset(line.getEndOffset()));
            }
        }
        else {
            //see if we can find start and end block comment tokens
            //if not, this will return a range from cursor to cursor (no-op)
            return getBlockComment(outer, editorAdaptor.getPosition().getModelOffset(), editorAdaptor);
        }
    }
    
    private StartEndTextRange getSingleLineComments(boolean outer, int offset, EditorAdaptor editorAdaptor) {
        TextContent model = editorAdaptor.getModelContent();
        CursorService cursorService = editorAdaptor.getCursorService();

        LineInformation lineOrig = model.getLineInformationOfOffset(offset);

        //we already know this first line matched,
        //we just have to see if adjacent lines also match
        LineInformation line = lineOrig;
        int start = offset;
        int end = line.getEndOffset();

        //search backwards for adjacent lines with single-line comments
        while(line.getNumber() > 0) {
            line = model.getLineInformation(line.getNumber() -1);
            String lineText = model.getText(line.getBeginOffset(), line.getLength());
            Matcher matcher = singleLine.matcher(lineText);
            if(matcher.find()) {
                offset = outer ? matcher.start(1) : matcher.end(1);
                start = line.getBeginOffset() + offset;
            }
            else {
                //hit a line without a comment token
                //use last good 'start'
                break;
            }
        }
        
        //prepare for forward search
        line = lineOrig;
        int totalLines = model.getNumberOfLines();
        
        //search forwards for adjacent lines with single-line comments
        while(line.getNumber() < totalLines) {
            line = model.getLineInformation(line.getNumber() +1);
            String lineText = model.getText(line.getBeginOffset(), line.getLength());
            Matcher matcher = singleLine.matcher(lineText);
            if(matcher.find()) {
                end = line.getEndOffset();
            }
            else {
                //hit a line without a comment token
                //use last good 'end'
                break;
            }
        }
        
        return new StartEndTextRange(
                cursorService.newPositionForModelOffset(start),
                cursorService.newPositionForModelOffset(end));
    }
    
    private StartEndTextRange getBlockComment(boolean linewise, int cursor, EditorAdaptor editorAdaptor) {
        TextContent model = editorAdaptor.getModelContent();
        CursorService cursorService = editorAdaptor.getCursorService();

        LineInformation lineOrig = model.getLineInformationOfOffset(cursor);

        //this current line may or may not have a comment token
        //make sure to check it before checking other lines
        LineInformation line = lineOrig;
        int start = cursor;
        int end = cursor;
        
        //search this line and backwards for a comment token
        while(true) {
            int lineStart = line.getBeginOffset();
            String lineText = model.getText(lineStart, line.getLength());
            Matcher matcher = blockStart.matcher(lineText);
            if(matcher.find()) {
                int offset = outer ? matcher.start(1) : matcher.end(1);
                if(offset == line.getLength()) {
                    //if comment token ended on a newline,
                    //use beginning of next line
                    line = model.getLineInformation(line.getNumber() +1);
                    start = line.getBeginOffset();
                }
                else {
                    start = lineStart + offset;
                }
                break;
            }
            
            matcher = blockEnd.matcher(lineText);
            if(matcher.find() && lineStart + matcher.end(1) < cursor) {
                //we found an end-comment token before the cursor,
                //don't enter that earlier comment
                return new StartEndTextRange(
                        cursorService.newPositionForModelOffset(cursor),
                        cursorService.newPositionForModelOffset(cursor));
            }

            if(line.getNumber() > 0) {
                //prepare for next iteration
                line = model.getLineInformation(line.getNumber() -1);
            }
            else {
                //give up, no start comment token found
                return new StartEndTextRange(
                        cursorService.newPositionForModelOffset(cursor),
                        cursorService.newPositionForModelOffset(cursor));
            }
        }

        //prepare for forward search
        line = lineOrig;
        int totalLines = model.getNumberOfLines();

        //search this line and forwards for a comment token
        while(true) {
            int lineStart = line.getBeginOffset();
            String lineText = model.getText(lineStart, line.getLength());

            Matcher matcher = blockEnd.matcher(lineText);
            if(matcher.find()) {
                int offset = outer ? matcher.end(1) : matcher.start(1);
                end = lineStart + offset;
                break;
            }
            
            matcher = blockStart.matcher(lineText);
            if(matcher.find() && lineStart + matcher.start(1) > start) {
                //we found a start-comment token after our start position,
                //don't enter that later comment
                //(using start instead of cursor here in case start and end
                // are on the same line and both are after the cursor)
                return new StartEndTextRange(
                        cursorService.newPositionForModelOffset(cursor),
                        cursorService.newPositionForModelOffset(cursor));
            }

            if(line.getNumber() < totalLines) {
                //prepare for next iteration
                line = model.getLineInformation(line.getNumber() +1);
            }
            else {
                //give up, no end comment token found
                return new StartEndTextRange(
                        cursorService.newPositionForModelOffset(cursor),
                        cursorService.newPositionForModelOffset(cursor));
            }
        }
        
        return new StartEndTextRange(
                cursorService.newPositionForModelOffset(start),
                cursorService.newPositionForModelOffset(end));
    }
    

    @Override
    public ContentType getContentType(Configuration configuration) {
        return outer && linewise ? ContentType.LINES : ContentType.TEXT;
    }

}

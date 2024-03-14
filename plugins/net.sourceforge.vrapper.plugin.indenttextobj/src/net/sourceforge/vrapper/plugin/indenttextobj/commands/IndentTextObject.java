package net.sourceforge.vrapper.plugin.indenttextobj.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractTextObject;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class IndentTextObject extends AbstractTextObject {

    public static final TextObject INNER_INNER = new IndentTextObject(false, false);
    public static final TextObject OUTER_INNER = new IndentTextObject(true, false);
    public static final TextObject OUTER_OUTER = new IndentTextObject(true, true);
    //INNER_OUTER is an alias to INNER_INNER

    private final boolean includeFirstLine;
    private final boolean includeLastLine;

    private IndentTextObject(boolean firstLine, boolean lastLine) {
        this.includeFirstLine = firstLine;
        this.includeLastLine = lastLine;
    }

    public ContentType getContentType(Configuration configuration) {
        return ContentType.LINES;
    }

    @Override
    public TextRange getRegion(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }

    	TextContent model = editorAdaptor.getModelContent();
    	LineInformation cursorLine = model.getLineInformationOfOffset(editorAdaptor.getPosition().getModelOffset());
    	
    	if(cursorLine.getLength() == 0) {
    		//if cursor is on an empty line,
    		//find the adjacent line (above or below)
    		//with the most indentation (skipping other empty lines)
    		int prevNum = cursorLine.getNumber() - 1;
    		LineInformation prev;
    		try {
    			while(prevNum > 0) {
    				if(model.getLineInformation(prevNum).getLength() > 0) {
    					break;
    				}
    				prevNum--;
    			}
    			prev = model.getLineInformation(prevNum);
    		}
    		catch(Exception e) {
    			prev = cursorLine;
    		}

    		int nextNum = cursorLine.getNumber() + 1;
    		LineInformation next;
    		try {
    			while(nextNum < model.getNumberOfLines()) {
    				if(model.getLineInformation(nextNum).getLength() > 0) {
    					break;
    				}
    				nextNum++;
    			}
    			next = model.getLineInformation(nextNum);
    		}
    		catch(Exception e) {
    			next = cursorLine;
    		}

            int prevIndent = VimUtils.getIndent(model, prev).length();
            int nextIndent = VimUtils.getIndent(model, next).length();
    		cursorLine = prevIndent > nextIndent ? prev : next;
    	}

    	//if no other lines match, default to current line
    	int indentLength = VimUtils.getIndent(model, cursorLine).length();
    	int start = cursorLine.getBeginOffset();
    	int end = cursorLine.getEndOffset();

    	int lineNo = cursorLine.getNumber() - 1;
    	LineInformation previousLine = cursorLine;
    	LineInformation line;
    	
    	//find previous line with different indent
    	while(lineNo >= 0) {
    		line = model.getLineInformation(lineNo);
    		if( (! VimUtils.isLineBlank(model, lineNo)) &&
    				VimUtils.getIndent(model, line).length() < indentLength) {
    			if(count == 1) {
    				start = includeFirstLine ? line.getBeginOffset() : previousLine.getBeginOffset();
    				break;
    			}
    			else {
    				indentLength = VimUtils.getIndent(model, line).length();
    				count--;
    			}
    		}
    		previousLine = line;
    		lineNo--;
    	}

    	//reset for next iteration
    	lineNo = cursorLine.getNumber() + 1;
    	previousLine = cursorLine;
    	int total = model.getNumberOfLines();
    	
    	//find next line with different indent
    	while(lineNo < total) {
    		line = model.getLineInformation(lineNo);
    		if( (! VimUtils.isLineBlank(model, lineNo)) &&
    				VimUtils.getIndent(model, line).length() < indentLength) {
    			end = includeLastLine ? line.getEndOffset() : previousLine.getEndOffset();
    			break;
    		}
    		previousLine = line;
    		lineNo++;
    	}

    	CursorService cursor = editorAdaptor.getCursorService();
    	Position startPos = VimUtils.fixLeftDelimiter(model, cursor, cursor.newPositionForModelOffset(start));
    	Position endPos = VimUtils.fixRightDelimiter(model, cursor, cursor.newPositionForModelOffset(end));
    	return new StartEndTextRange(startPos, endPos);
    }

}
package org.semantictools.context.view;

import java.io.FileWriter;
import java.io.PrintWriter;

public class PrintContext {

	private boolean isFileWriter;
	private PrintWriter writer;
	private int indent = 0;
	
	public boolean isFileWriter() {
		return isFileWriter;
	}
	
	public PrintWriter getWriter() {
		return writer;
	}
	
	public void setFileWriter(FileWriter writer) {
	  this.writer = new PrintWriter(writer);
	  isFileWriter = true;
	}
	public void setWriter(PrintWriter writer) {
		this.writer = writer;
		isFileWriter = false;
	}


	protected void pushIndent() {
		indent++;
	}
	
	protected void popIndent() {
		indent--;
	}
	public int getIndent() {
		return indent;
	}
	
	public void setIndent(int indent) {
	  this.indent = indent;
	}
	
	

}

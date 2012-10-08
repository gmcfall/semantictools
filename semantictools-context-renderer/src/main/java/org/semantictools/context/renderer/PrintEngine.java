package org.semantictools.context.renderer;

import java.io.PrintWriter;

public class PrintEngine {
	protected PrintContext context;
	
	
	public PrintEngine() {}
	
	public PrintEngine(PrintContext context) {
	  this.context = context;
	}

	protected void setWriter(PrintWriter writer) {
		context.setWriter(writer);
	}
	
	protected PrintWriter getWriter() {
		return context.getWriter();
	}
	
	protected PrintContext getPrintContext() {
	  return context;
	}
	
	protected PrintEngine print(String s) {
		context.getWriter().print(s);
		return this;
	}
  
    protected void printAttr(String name, String value) {
      print(" ");
      print(name);
      print("=\"");
      print(value);
      print("\"");
    }
    
    protected void print(int n) {
      context.getWriter().print(n);
    }

	protected void println() {
		context.getWriter().println();
		
	}
	protected void println(String s) {
		context.getWriter().println(s);
		
	}

	protected PrintEngine pushIndent() {
		context.pushIndent();
		return this;
	}

	protected PrintEngine popIndent() {
		context.popIndent();
		return this;
	}
	
	protected PrintEngine indent() {
		int indent = context.getIndent();
		for (int i=0; i<2*indent; i++) {
			context.getWriter().print(' ');
		}
		return this;
	}
	
	protected void flush() {
		context.getWriter().flush();
	}
	
	protected void close() {
		PrintWriter writer = context.getWriter();
		writer.flush();
		if (context.isFileWriter())	writer.close();
		context.setWriter(null);
	}
	

}

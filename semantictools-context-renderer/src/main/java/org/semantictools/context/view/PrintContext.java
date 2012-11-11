/*******************************************************************************
 * Copyright 2012 Pearson Education
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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

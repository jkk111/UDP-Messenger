package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	private BufferedWriter in;
	private BufferedWriter out;
	private BufferedWriter err;
	
	public Logger() {
		try {
		File f = new File("logs/");
		f.mkdirs();
		String filename =  Thread.currentThread().getStackTrace()[2].getFileName();
		filename = filename.substring(0, filename.length() - 5);
		SimpleDateFormat openTime = new SimpleDateFormat("dd-M-yyyy-hh-mm-ss");
		in = new BufferedWriter(new FileWriter("logs/" + filename + "_" + openTime.format(new Date()) + "_input.log"));
		out = new BufferedWriter(new FileWriter("logs/" + filename + "_" + openTime.format(new Date()) + "_output.log"));
		err = new BufferedWriter(new FileWriter("logs/" + filename + "_" + openTime.format(new Date()) + "_error.log"));
		StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
		String line = trace.getFileName().substring(0,trace.getFileName().length()-5);
		line += "." + trace.getMethodName() + "." + trace.getLineNumber()+") ";
		in.write(line + "Started at: " + openTime.format(new Date()));
		out.write(line + "Started at: " + openTime.format(new Date()));
		err.write(line + "Started at: " + openTime.format(new Date()));
		} catch (IOException e) {
			System.err.println("There was an error creating the needed files");
		}
	}
	
	public void in(String text) {
		StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
		String line = trace.getFileName().substring(0,trace.getFileName().length()-5);
		try {
			in.write(line + "." + trace.getMethodName() + "." + trace.getLineNumber() + ") " + text +'\r'+'\n');
		} catch (IOException e) {
			System.err.println("error writing input to file");
		}
		System.out.println("in) " + text);
	}
	
	public void out(String text) {
		StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
		String line = trace.getFileName().substring(0,trace.getFileName().length()-5);
		try {
			out.write(line + "." + trace.getMethodName() + "." + trace.getLineNumber() + ") " + text +'\r'+'\n');
		} catch (IOException e) {
			System.err.println("error writing output to file");
		}
		System.out.println("out) " + text);
	}
	
	public void err(String text) {
		StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
		String line = trace.getFileName().substring(0,trace.getFileName().length()-5);
		try {
			err.write(line + "." + trace.getMethodName() + "." + trace.getLineNumber() + ") " + text +'\r'+'\n');
		} catch (IOException e) {
			System.err.println("error writing error to file");
		}
		System.err.println("err) " + text);
	}
	
	public void close() {
		SimpleDateFormat closeTime = new SimpleDateFormat("dd-M-yyyy_hh:mm:ss");
		StackTraceElement trace = Thread.currentThread().getStackTrace()[2];
		String line = trace.getFileName().substring(0,trace.getFileName().length()-5);
		line += "." + trace.getMethodName() + "." + trace.getLineNumber()+") ";
		try {
			in.write(line + "Closing window at: " + closeTime.format(new Date()));
			out.write(line + "Closing window at: " + closeTime.format(new Date()));
			err.write(line + "Closing window at: " + closeTime.format(new Date()));
			in.close();
			out.close();
			err.close();
		} catch (IOException e) {
			System.err.println("error closing files");
		}
		System.out.print("done");
	}
}

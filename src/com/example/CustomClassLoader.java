package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;

public class CustomClassLoader extends ClassLoader{
	
	//读取一个文件内容
	private byte[] getBytes(String name) throws IOException {
		File file = new File(name);
		long len = file.length();
		byte[] raw = new byte[(int)len];
		FileInputStream fin = new FileInputStream(file);
		
		//一次性读取class文件的全部二进制数据
		int r = fin.read(raw);
		fin.close();
		if(r != len) {
			throw new IOException("无法读取全部文件：" + r + " != " + len);
		}
		
		return raw;
	}
	
	//定义编译指定Java文件的方法
	private boolean compile(String javaFile) throws IOException {
		System.out.println("CustomClassLoader:正在编译" + javaFile + "...");
		//调用系统javac命令
		Process processJavac = Runtime.getRuntime().exec("javac " + javaFile);
		try {
			processJavac.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			return false;
		}
		
		int ret = processJavac.exitValue();
		
		return 0 == ret;
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		Class clazz = null;
		
		String fileStub = name.replace(".", "/");
		String javaFilename ="src/" + fileStub + ".java";
		String classFilename ="src/" +  fileStub + ".class";
		File javaFile = new File(javaFilename);
		File classFile = new File(classFilename);
		
		//System.out.println(javaFile.getAbsolutePath());  //找到文件的绝对路径
		
		//删除class文件，测试编译java文件
		Process processRm;
		try {
			processRm = Runtime.getRuntime().exec("rm " + classFilename);
			processRm.waitFor();
		} catch (IOException | InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		if(javaFile.exists() && (!classFile.exists() || javaFile.lastModified() > classFile.lastModified())) {
			try {
				if(!compile(javaFilename) || !classFile.exists()) {
					throw new ClassNotFoundException("ClassNotFoundException:" + javaFilename);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				return null;
			}
		}
		
		if(classFile.exists()) {
			try {
				byte[] raw = getBytes(classFilename);
				clazz = defineClass(null, raw, 0, raw.length);
			}catch (IOException e) {
				return null;
			}
		}
		
		if(null == clazz) {
			throw new ClassNotFoundException(name);
		}
		
		return clazz;
	}
	
	//定义一个主方法
	public static void main(String[] args) throws Exception {
		
		Class<?> clazz = new CustomClassLoader().loadClass("com.example.haha");
		Method method = clazz.getDeclaredMethod("sayHaha");
		method.invoke(null);
	}

}

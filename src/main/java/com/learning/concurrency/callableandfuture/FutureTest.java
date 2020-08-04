package com.learning.concurrency.callableandfuture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * 我们通过这个例子来认识一下 Callable 和 Future:
 *  1. Runnable 封装了一个异步运行的任务，可以把他想象成一个没有参数和返回值的异步方法，而 Callable 则封装了一个可以返回指定类型结果
 *     的异步运行任务。
 *  2. 而 Future 则用来保存异步运算的结果，我们主要使用它约定的阻塞方法 get()
 *  3. Callable 和 Future 都是接口，本例我们将使用 FutureTask 这个包装器将 Callable 转换为 Future 和 Runnable，他同时实现二者的
 *     接口。
 *
 * 你会看到一个稍微复杂点的“多线程”，因为他们中的一部分是递归生成的，当然我们得到的最终结果也是“递归”统计的
 * 我们想要得到的结果是指定目录下含有指定关键字的文件的个数
 */
public class FutureTest {

    public static void main(String[] args){
        /**
         * 在jdk 1.7之后出现了带资源的try语句，它允许在try关键字后紧跟一对圆括号，圆括号可以声明、初始化一个或多个资源（此处的资源是
         * 指那些必须在程序结束时显式关闭的资源，比如数据库连接，网络连接等），try-with-resources 是一个定义了一个或多个资源的try
         * 声明，try语句在该语句结束时自动关闭这些资源。try-with-resources确保每一个资源在处理完成后都会被关闭。这些资源必须实现
         * AutoCloseable或者Closeable接口，实现这两个接口就必须实现close() 方法。
         *
         *  jdk 1.7之后出现的一个新的语句，主要用于关闭资源。所谓资源，就是一个流，一个连接，一个数据库连接等。
         *
         *  在原来关闭资源的时候，用 try-catch-finally 时如果try中的代码跑出了一个非 IOException，在执行finally调用close方法时
         *  close方法本身也会有可能抛出 IOException 异常。这种情况下，原始的异常将丢失，转而抛出close方法的异常。
         */
        try(Scanner in = new Scanner(System.in)){
            System.out.println("Enter a directory: ");
            String directory = in.nextLine();
            System.out.println("Enter a keyword ：");
            String keyword = in.nextLine();

            MatchCounter matchCounter = new MatchCounter(new File(directory), keyword);
            FutureTask<Integer> task = new FutureTask<>(matchCounter);
            Thread t = new Thread(task);
            t.start();
            try
            {
                System.out.println(task.get() + "matching files.");
            }catch (InterruptedException e)
            {
            }catch (ExecutionException e)
            {
                e.printStackTrace();
            }
        }
    }
}

class MatchCounter implements Callable<Integer> {

    private File directory;
    private String keyword;

    public MatchCounter(File directory, String keyword){
        this.directory = directory;
        this.keyword = keyword;
    }

    public Integer call(){
        int count = 0;
        File[] files = directory.listFiles();
        List<Future<Integer>> results = new ArrayList<>();
        try
        {
            for(File file : files){
                if(file.isDirectory()){
                    MatchCounter counter = new MatchCounter(file, keyword);
                    FutureTask<Integer> task = new FutureTask<>(counter);
                    // !!! 注意 task.get() 返回值类型是 Integer 不是 Future<Integer>
                    results.add(task);
                    Thread t = new Thread(task);
                    t.start();
                }
                else{
                    if(search(file, keyword))
                        count ++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        for(Future<Integer> result : results){
            try {
                count += result.get();
            }catch (InterruptedException e){
            }catch (ExecutionException e){
                e.printStackTrace();
            }
        }
        return count;
    }

    // 有问题，没有考虑 IOException
    private boolean search(File file, String keyword) throws Exception{
        boolean contains = false;
        Scanner in = new Scanner(file, "UTF-8");
        while(!contains && in.hasNextLine()){
            String line = in.nextLine();
            if(line.contains(keyword))
                contains = true;
        }
        return contains;
    }
}

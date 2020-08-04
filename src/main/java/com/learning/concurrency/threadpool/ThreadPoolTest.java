package com.learning.concurrency.threadpool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/***
 * 本例使用线程池处理指定目录下的关键字文件匹配任务
 */
public class ThreadPoolTest {
    public static void main(String[] args){

        try(Scanner in = new Scanner(System.in)) {
            System.out.println("Enter a directory :");
            String directory = in.nextLine();
            System.out.println("Enter a keyword :");
            String keyword = in.nextLine();

            ExecutorService pool = Executors.newCachedThreadPool();
            Countor countor = new Countor(new File(directory), keyword, pool);

            // 下面这行代码没有语法错误，因为 FutureTask 同时实现了 Future 和 Runnable，他可以将 Callable 转换为二者
            // FutureTask 实现了 Runnable，所以他可以作为线程创建时的参数
            // FutureTask 实现了 Future，所以他具有了 get() 方法
            // 但是，Future 是干嘛的，是保存运算结果的!!!
            // 所以，下面这句代码有啥意义呢？不知道是我还没见识到，还是下面这句代码属实么的意义 ^_^
            //Future<Integer> task = new FutureTask<>(countor);

            Future<Integer> result = pool.submit(countor);
            try {
                System.out.println(result.get() + " matching files");
            }catch (ExecutionException e){
                e.printStackTrace();
            }catch (InterruptedException e){

            }

            pool.shutdown();
            int largestPoolSize = ((ThreadPoolExecutor)pool).getLargestPoolSize();
            System.out.println("the largestPoolSize is : " + largestPoolSize);
        }

    }
}

class Countor implements Callable<Integer>{

    private File dir;
    private String key;
    private ExecutorService pool;

    public Countor(File dir, String key, ExecutorService pool){
        this.dir = dir;
        this.key = key;
        this.pool = pool;
    }

    public Integer call(){
        int count = 0;
        try {
            File[] files = dir.listFiles();
            List<Future<Integer>> results = new ArrayList<>();

            for (File file : files) {
                if (file.isDirectory()) {
                    Countor newtask = new Countor(file, key, pool);
                    Future<Integer> result = pool.submit(newtask);
                    results.add(result);
                } else {
                    if (search(file))
                        count++;
                }
            }

            for (Future<Integer> res : results) {
                try {
                    count += res.get();
                }catch (ExecutionException e){
                    e.printStackTrace();
                }
            }
        }catch (InterruptedException e){

        }
        return count;
    }

    private boolean search(File file){
        //boolean fond = false;
        try {
            try (Scanner in = new Scanner(file, "UTF-8")) {
                boolean fond = false;
                while (!fond && in.hasNextLine()) {
                    String line = in.nextLine();
                    if (line.contains(key))
                        fond = true;
                }
                return fond;
            }
        }catch (IOException e){
            return false;
        }
    }
}

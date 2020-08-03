package com.learning.concurrency.blockingqueue;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 在这里，我们将使用队列这个数据结构作为一种同步机制，实现对一组线程的控制
 * 概念：
 *  1. 阻塞队列：实现数据在生产者线程与消费者线程之间的传递
 *  2. 生产者线程集：收集元素并将其放入阻塞队列
 *  3. 消费者线程集：从阻塞队列取出元素并处理
 * 具体做了什么：
 *  生产者线程枚举在所有子目录下的文件并把他们放到一个阻塞队列；
 *  同时启动了大量的搜索线程（消费者线程），每个搜索线程从队列中取出一个文件，打开它，打印所有包含指定关键字的行，然后取出下一个文件。
 * 思考：
 *  1. 队列要起到阻塞队列的作用，就要有能够阻塞线程的能力，而不是抛个异常或者提示一下错误信息就溜之大吉了，它得协助线程把活干完，所以，
 *     我们要用队列的“阻塞方法”：put() and take()
 *  2. 当一个线程去空阻塞队列中“take”元素时，他是要被阻塞的，那如何让众多的消费者线程优雅地把活干完，而不是在活干完之后就在那阻塞着呢？
 *  3. 我在刚接触阻塞队列这个概念时有一个疑问，put() 和 take() 方法是“原子操作”吗？如果不是，那阻塞队列岂不是失去了意义？就比如，假设
 *     take() 方法分为“取”和“删”两个原子操作，一个线程执行到“取”，还没“删”，他就被剥夺了时间片，然后另一个线程又去队列头“取”了一次，
 *     那这两个线程不就处理了同一个元素了吗？也就不安全了？-- 这个问题在接触“线程安全”之后也就解决了。所有并发的队列实现都是线程安全的。
 *     事实上，在他们的源码中可以看到，其实他们就是在 take() 和 put() 方法中加了“锁”。
 */
public class BlockingQueueTest {

    private static final int FILE_QUEUE_SIZE = 10;
    // 这里我们不考虑 ArrayBlockingQueue 中的“公平性”
    private static final BlockingQueue<File> queue = new ArrayBlockingQueue<>(FILE_QUEUE_SIZE);
    private static final int SEARCH_THREADS_SIZE = 100;
    // 所有“文件”都被处理完之后， take() 空队列的线程该如何优雅地结束，这里可以使用一个非 null 但是为“空”的 File 对象
    private static final File DUMMY = new File("");

    public static void main(String[] args){
        try(Scanner in = new Scanner(System.in))
        {
            System.out.println("Enter a base directory (e.g. /opt/jdk1.8.0/src) :");
            String directory = in.nextLine();
            System.out.println("Enter a keyword (e.g. volatile) :");
            String keyword = in.nextLine();

            // 生产者线程的 run() 方法
            Runnable enumerator = () -> {
                try
                {
                    enumerate(new File(directory));
                    queue.put(DUMMY);
                }catch (InterruptedException e)
                {
                }
            };
            new Thread(enumerator).start();

            for(int i = 1; i <= SEARCH_THREADS_SIZE; i++){
                Runnable searcher = () -> {
                    try
                    {
                        boolean done = false;
                        while(!done){
                            File file = queue.take();
                            if(file == DUMMY){
                                done = true;
                                // 记得把 “DUMMY” 放回去，因为这不一定是最后一个线程
                                queue.put(DUMMY);
                            }
                            else {
                                search(file, keyword);
                            }
                        }
                    }catch (InterruptedException e)
                    {
                    }
                };
                new Thread(searcher).start();
            }
        }
    }

    private static void enumerate(File directory) throws InterruptedException{
        File[] files = directory.listFiles();
        for( File file : files){
            if(file.isDirectory())
                enumerate(file);
            else
                queue.put(file);
        }
    }

    private static void search(File file, String keyword){
        try {
            int lineNumber = 0;
            Scanner in = new Scanner(file, "UTF-8");
            while(in.hasNextLine()){
                lineNumber ++;
                String line = in.nextLine();
                if(line.contains(keyword)){
                    System.out.printf("%s:%d:%s%n", file.getPath(), lineNumber, line);
                }
            }
        }catch (FileNotFoundException e){
        }
    }
}

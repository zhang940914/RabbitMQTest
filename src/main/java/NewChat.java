

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class NewChat {
	static Scanner scanners =null ;
	public static void main(String[] args) {
		System.out.print("请输入名字：");
		 scanners = new Scanner(System.in);
		commonCode.userName = scanners.next();
		
		
		server server = new server();
		
		service service = new service();
		
		Thread thread1 = new Thread(service,commonCode.userName);
		
		Thread thread2 = new Thread(server,commonCode.userName);
		
		thread1.start();
		thread2.start();
	}
}

//发送端
class  server implements Runnable{
	Scanner scanner = null;
	public void run() {
		
		//创建了一个fanout的交换机
		commonCode.commonMethod();
	while(true){
		System.out.println("请输入要发送的消息：");
		//发送的消息
		 scanner = new Scanner(System.in);
		
		String message = scanner.next();
		
		try {
			//发送消息到队列中basicPublish("交换机名称",routingkey, , 发送到队列的消息)
			commonCode.channel.basicPublish(commonCode.EXCHANGE_NAME, 
					"", 
					null, 
					message.getBytes()
					);
			/*System.out.println(commonCode.userName);*/
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		}
		
	
		//关闭channel  connnection
	/*finally{
		try {
			if(commonCode.channel != null){
			commonCode.channel.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		} catch (TimeoutException e) {
			e.printStackTrace();
			throw new RuntimeException(e);

		}
		finally{
			try {
				commonCode.connection.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);

			}
		}
	}*/
		
	}	
		
	}
	
}

//接收端
class service implements Runnable{

	public void run() {
		//创建一个fanout的交换机
		commonCode.commonMethod();
		try {
			//创建一个非持久化、独立、自动删除的队列名称。
		String queueName = commonCode.channel.queueDeclare().getQueue();
			//将交换机和队列绑定
		commonCode.channel.queueBind(queueName, commonCode.EXCHANGE_NAME, "");
			
		QueueingConsumer consumer = new QueueingConsumer(commonCode.channel);
		
		commonCode.channel.basicConsume(queueName, true, consumer);
		
		while(true){
			 try {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				
				@SuppressWarnings("deprecation")
				String message = new String(delivery.getBody());
				System.out.println(commonCode.userName + "发送了： '" + message + "'--"); 
			 
			 
			 } catch (ShutdownSignalException e) {
				e.printStackTrace();
				throw new RuntimeException(e);

			} catch (ConsumerCancelledException e) {
				e.printStackTrace();
				throw new RuntimeException(e);

			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e);

			}
			 
			 
		}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);

			
		}
		
	}
	
}

//公共代码
class commonCode{
	
		//定义一个交换机名称
		 static final String EXCHANGE_NAME = "fanout";
		//路由名称    fanout 模式不需要RouteKey
		 static final String ROUTING_KEY_NAME = "rout";

		//工厂
		static ConnectionFactory factory = null;
		//连接
		static Connection connection = null;
		//通道
		static Channel channel = null;
		//用户名
		static String userName = null;
		//发送的信息
		static String  message = null;
		static Scanner scanner = null;
		static QueueingConsumer consumer=null;
		static String receiveMessage=null;
		static QueueingConsumer.Delivery delivery=null;
	
	public static void commonMethod(){
		
		factory = new ConnectionFactory();
		//设置其参数
		//MQ的IP
		factory.setHost("127.0.0.1");
		//MQ端口 
		factory.setPort(5672);
		//MQ用户名
		factory.setUsername("guest");
		//mq密码
		factory.setPassword("guest");
		
		try {
			//创建连接
			connection =  factory.newConnection();
			//创建通道
			channel = connection.createChannel();
			//使用【fanout】类型创建一个名称为 fanout的交换器，
			channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
			
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
			
		}
		
	}
	
}










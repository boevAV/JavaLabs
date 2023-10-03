import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class BuyersThread implements Runnable {
    private Buyer buyer;//Класс реализующий функции покупателя

    private Semaphore fillingPumps;//Счётный семафор = контроллирующий доступ к заправочным насосам

    private BlockingQueue<FOrder> queueCheckout;//Блокирующая очередь = моделирующая очередь в кассу

    private BlockingQueue<Semaphore> cashierJob;//Блокирующая очередь = моделирующая работу кассира

    private Semaphore refuelingProcess;//Семафор, моделирующий процесс заправки

    //Конструктор потока клиента
    public BuyersThread(Semaphore fPumps, BlockingQueue<FOrder> qCheckout, BlockingQueue<Semaphore> rProcess)
    {
        buyer = new Buyer();
        fillingPumps = fPumps;
        queueCheckout = qCheckout;
        cashierJob = rProcess;
    }

    @Override
    public void run() {//моделирует поведение клиента на заправочной станции, включая ожидание доступа к
        // насосу, оплату, заправку и освобождение насоса для других клиентов.
        buyer.setBuyer(getCustomerName());//Устанавливается имя покупателя
        while (!Thread.currentThread().isInterrupted()) {//Бесконечный цикл, который выполняется до тех
            // пор, пока текущий поток покупателя не будет прерван.
            try {
                fillingPumps.acquire();//Покупатель ожидает, пока освободится заправочный насос,
                // используя семафор fillingPumps для синхронизации доступа к насосам.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(buyer.getBuyer() + " has taken over the fuel pump");//Выводится сообщение
            // о том, что покупатель занял топливный насос
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int sum = buyer.pay();
            String fuelTypeName = buyer.getFType().getName();
            System.out.println(buyer.getBuyer() + " has paid for fuel: Fuel " + fuelTypeName + "; Sum " + sum);
            //Покупатель выполняет оплату и создает заказ, затем помещает заказ в очередь чекаута
            try {
                queueCheckout.put(buyer.makeNFOrder(sum));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Покупатель ожидает, когда кассир активирует топливный насос для его автомобиля
            try {
                refuelingProcess = cashierJob.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Покупатель ожидает, пока его автомобиль будет заправлен
            try {
                refuelingProcess.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(buyer.getBuyer() + " has finished refueling the car");//После заправки
            // выводится сообщение о завершении заправки
            try {
                Thread.sleep(1000);//Клиент ожидает некоторое время (1 секунда),
                fillingPumps.release();//затем освобождает топливный насос, позволяя другим клиентам
                // использовать его.
                System.out.println(buyer.getBuyer() + " has vacated the fuel pump");//После этого
                // выводится сообщение о том, что клиент освободил насос
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getCustomerName() {//Метод, получающий имя покупателя
        String name = "Buyer" + Thread.currentThread().getName().substring(6);
        return name;
    }
}
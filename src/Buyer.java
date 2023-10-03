import java.util.Random;

public class Buyer {
    private FType fType;//Тип топлива автомобила покупателя

    private String nBuyer;//Сумма, на которую покупатель хотел бы заправить автомобиль

    public Buyer() {//Конструктор класса, позволяющий выбрать тип топлива
        switch (new Random().nextInt(3)) {
            case 0:
                setFType(FType.FUEL_92);
                break;
            case 1:
                setFType(FType.FUEL_95);
                break;
            case 2:
                setFType(FType.FUEL_DIESEL);
                break;
            default:
                break;
        }
    }

    public Buyer(FType type, String name) {//Конструктор класса
        fType = type;
        nBuyer = name;
    }

    public FType getFType() {//Метод, возвращающий тип топлива автомобиля покупателя
        return fType;
    }

    public void setFType(FType type) {//Метод, задающий тип топлива автомобиля покупателя
        fType = type;
    }

    public String getBuyer() {//Метод, возвращающий имя покупателя
        return nBuyer;
    }

    public void setBuyer(String name) {//Метод, задающий имя покупателя
        nBuyer = name;
    }

    public FOrder makeNFOrder(int sum) {//Метод, деляющий новый заказ на заправочной станции
        return new FOrder(fType, sum);
    }

    public int pay() {//Метод, который генерирует случайную сумму денег, которая может быть использована
        // для оплаты заказа топлива, с учетом цены топлива и некоторой случайной вариации.
        Random rand = new Random();
        int sum = (20 + rand.nextInt(10)) * fType.getPrice();//Генерируется случайная сумма (sum):
        //генерирует случайное целое число от 0 до 9
        //Затем к этому случайному числу добавляется 20 (20 + rand.nextInt(10)), что создает случайное
        // число от 20 до 29.
        //Это случайное число затем умножается на цену единицы топлива (fType.getPrice()), чтобы
        // получить общую сумму, которую клиент хочет потратить на заказ.
        return sum;
    }
}

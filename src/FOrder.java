public class FOrder {
    private FType fType;//Тип топлива заказа
    private int oSum;//Сумма заказа

    public FOrder(FType type, int sum) { //Конструктор класса
        fType = type;           //тип топлива
        this.oSum = sum;        //сумма заказа
    }

    public FType getFType() {//Метод, возвращающий тип топлива заказа
        return fType;
    }

    public void setFType(FType type) {//Метод задания типа топлива в заказе
        this.fType = type;
    }

    public int getSum() {//Метод, возвращающий сумму заказа
        return oSum;
    }

    public void setSum(int oSum) {//Метод задания суммы заказа
        this.oSum = oSum;
    }
}

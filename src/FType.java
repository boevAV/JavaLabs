public enum FType {
    //Виды топлива на станции:
    FUEL_92(55, "AI92"),
    FUEL_95(63, "AI95"),
    FUEL_DIESEL(76, "DT");
    private int price;//Цена за литр топлива

    private String name;//Название типа топлива

    FType(int price, String name) {//Конструктор класса
        this.price = price;
        this.name = name;
    }

    public int getPrice() {//Метод, возвращающий цену за литр топлива
        return price;
    }

    public String getName() {//Метод, возвращающий тип топлива
        return name;
    }
}

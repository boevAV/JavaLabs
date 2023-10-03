import java.math.BigDecimal;
import java.util.function.Function;

public class CalcEquation {
    final Function<CalcVector, BigDecimal>[] arrFunctions;//Массив функций
    BigDecimal x_0;//Начальное значение x
    CalcVector y_0;//Начальное значение y
    BigDecimal step;//Значение шага
    BigDecimal maxX;//Максимальное значение

    //Конструктор класса
    CalcEquation(final Function<CalcVector, BigDecimal>[] arrFunctions, BigDecimal x_0, CalcVector y_0, BigDecimal step, BigDecimal maxX) {
        this.arrFunctions = arrFunctions;
        this.x_0 = x_0;
        this.y_0 = y_0;
        this.step = step;
        this.maxX = maxX;
    }

    //Конструктор класса
    public CalcEquation(CalcEquation eq) {
        arrFunctions = eq.getArrFunctions();
        x_0 = eq.getX_0();
        y_0 = eq.getY_0();
        step = eq.getStep();
        maxX = eq.getMaxX();
    }

    void setStep(BigDecimal newStep) {//Метод, задающий значение шага
        step = newStep;
    }

    Function<CalcVector, BigDecimal>[] getArrFunctions() {//Метод, возвращающий массив функций
        return arrFunctions;
    }

    BigDecimal getX_0() {//Метод, возвращающий начальное значение x
        return x_0;
    }

    CalcVector getY_0() {//Метод, возвращающий начальное значение y
        return y_0;
    }

    BigDecimal getStep() {//Метод, возвращающий шаг
        return step;
    }

    BigDecimal getMaxX() {//Метод, возвращающий максимальное значение
        return maxX;
    }
}


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

public class CalcTest extends TestCase
{
    static int SCALE = 2;

    public CalcTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( CalcTest.class );
    }

    public static boolean equals(BigDecimal[] results, BigDecimal[] etalon) {
        return Arrays.equals(results, etalon, (o1, o2) -> {

            MathContext mc = new MathContext(SCALE);
            return o1.round(mc).compareTo(o2.round(mc));
        });
    }

    public void check11() {
        CalcEquation eq = Main.getEquation1();
        BigDecimal[] lastY = {
                BigDecimal.valueOf(0.5518), BigDecimal.valueOf(-0.1839)
        };

        assertTrue(equals(
                lastY, Main.resolve(eq, Main.SetMode.DISABLE).get(new BigDecimal("0.50")).getElVector()
        ));
    }

    public void check12() {
        CalcEquation eq = Main.getEquation1();
        BigDecimal[] lastY = {
                BigDecimal.valueOf(0.5518), BigDecimal.valueOf(-0.1839)
        };
        assertTrue(equals(
                lastY, Main.resolve(eq, Main.SetMode.ENABLE).get(new BigDecimal("0.50")).getElVector()
        ));
    }

    public void check21() {
        CalcEquation eq = Main.getEquation2();
        BigDecimal[] lastY = {
                BigDecimal.valueOf(0.1353), BigDecimal.valueOf(-9.2046e-10), BigDecimal.valueOf(12.4445), BigDecimal.valueOf(0.5622)
        };
        assertTrue(equals(
                lastY, Main.resolve(eq, Main.SetMode.DISABLE).get(new BigDecimal("1.00")).getElVector()
        ));
    }

    public void check22() {
        CalcEquation eq = Main.getEquation2();
        BigDecimal[] lastY = {
                BigDecimal.valueOf(0.1353), BigDecimal.valueOf(-9.2046e-10), BigDecimal.valueOf(12.4445), BigDecimal.valueOf(0.5622)
        };
        assertTrue(equals(
                lastY, Main.resolve(eq, Main.SetMode.ENABLE).get(new BigDecimal("1.00")).getElVector()
        ));
    }

    public void testApp()
    {
        check11();
        check12();
        check21();
        check22();
    }
}

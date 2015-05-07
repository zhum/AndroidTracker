package uz.droid.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Sarimsakov Bakhrom Azimovich on 25.09.13 15:28.
 */

@Target(value= ElementType.FIELD)
@Retention(value= RetentionPolicy.RUNTIME)
public @interface Column {
    String name();
    int columnOrder() default Integer.MAX_VALUE;
}

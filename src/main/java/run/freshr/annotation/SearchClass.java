package run.freshr.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Search Annotation.
 *
 * @author FreshR
 * @apiNote Get Parameter VO 클래스에 적용할 Annotation
 * @since 2023. 1. 12. 오후 2:42:05
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface SearchClass {

  /**
   * 공통 필드를 정의한 공통(또는 추상) 클래스 여부
   *
   * @return the boolean
   * @apiNote 공통 필드를 정의한 공통(또는 추상) 클래스 여부<br>
   * 공통 필드를 정의한 클래스가 아니라면 따로 작성하지 않아도 됨
   * @author FreshR
   * @since 2023. 1. 12. 오후 2:42:05
   */
  boolean base() default false;

  /**
   * 공통 필드를 정의한 공통(또는 추상) 클래스 상속 여부
   *
   * @return the boolean
   * @apiNote 공통 필드를 정의한 공통(또는 추상) 클래스 상속 여부<br>
   * 공통 필드를 정의한 클래스가 아니라면 따로 작성하지 않아도 됨
   * @author FreshR
   * @since 2023. 1. 12. 오후 2:42:05
   */
  boolean extend() default true;

}

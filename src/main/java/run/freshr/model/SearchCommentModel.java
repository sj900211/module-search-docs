package run.freshr.model;

import lombok.Builder;
import lombok.Data;

/**
 * SearchComment 모델
 *
 * @author FreshR
 * @apiNote SearchComment Annotation 을 적용한 클래스의 정보를<br>
 * 가공하기 편리하도록 정의한 클래스
 * @since 2023. 1. 12. 오후 2:50:59
 */
@Data
@Builder
public class SearchCommentModel {

  /**
   * 필드 이름
   *
   * @apiNote 필드 이름
   * @since 2023. 1. 12. 오후 2:50:59
   */
  private String field;
  /**
   * 대상 필드 이름
   *
   * @apiNote 대상 필드 이름<br>
   * 필드 이름과 대상 필드 이름을 분리한 이유는 Generic 유형에 대한 고려<br>
   * 필드 유형이 'ID' 라면 Long, String, Integer 세 개의 필드를 생성
   * @since 2023. 1. 12. 오후 2:50:59
   */
  private String name;
  /**
   * 대상 필드 유형
   *
   * @apiNote 대상 필드의 데이터 타입
   * @since 2023. 1. 12. 오후 2:50:59
   */
  private String type;
  /**
   * 대상 필드의 서브 유형
   *
   * @apiNote List 유형에 대해 Generic 데이터 타입을 작성<br>
   * 하지만 다른 유형이나 뎁스가 더 늘어나는 항목에 대한 고려가 안되어 있음
   * @since 2023. 1. 12. 오후 2:50:59
   */
  private String subType;
  /**
   * 데이터 포맷
   *
   * @apiNote DateTimeFormat 이 있는 경우 해당 패턴 값을 읽어서 저장
   * @since 2023. 1. 12. 오후 2:50:59
   */
  private String format;
  /**
   * 설명
   *
   * @apiNote 필드에 대한 설명
   * @since 2023. 1. 12. 오후 2:50:59
   */
  private String comment;
  /**
   * List 여부
   *
   * @apiNote List 유형인지 여부
   * @since 2023. 1. 12. 오후 2:50:59
   */
  private Boolean listFlag;

}

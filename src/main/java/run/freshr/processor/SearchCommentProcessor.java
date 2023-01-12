package run.freshr.processor;

import static java.util.Objects.isNull;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.FIELD;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.NOTE;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.springframework.format.annotation.DateTimeFormat;
import run.freshr.annotation.SearchClass;
import run.freshr.annotation.SearchComment;
import run.freshr.model.SearchCommentModel;
import run.freshr.model.SearchCommentModel.SearchCommentModelBuilder;

/**
 * Process
 *
 * @author FreshR
 * @apiNote Compile 에서 동작할 Process 정의
 * @since 2023. 1. 12. 오후 2:59:33
 */
@AutoService(Processor.class)
public class SearchCommentProcessor extends AbstractProcessor {

  /**
   * 지원 Annotation 유형 설정
   *
   * @return the supported annotation types
   * @apiNote 지원 Annotation 유형 설정
   * @author FreshR
   * @since 2023. 1. 12. 오후 2:59:34
   */
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(SearchComment.class.getName());
  }

  /**
   * 지원 소스 버전 설정
   *
   * @return the supported source version
   * @apiNote 지원 소스 버전 설정
   * @author FreshR
   * @since 2023. 1. 12. 오후 2:59:34
   */
  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  /**
   * Process
   *
   * @param annotations the annotations
   * @param roundEnv    the round env
   * @return the boolean
   * @apiNote Compile 에서 동작할 로직 정의
   * @author FreshR
   * @since 2023. 1. 12. 오후 2:59:34
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    try {
      if (annotations.isEmpty()) {
        return false;
      }

      processingEnv.getMessager().printMessage(NOTE, "SearchCommentProcessor.process");

      List<SearchCommentModel> baseFieldList = new ArrayList<>();

      for (Element element : roundEnv.getRootElements()) {
        // 클래스 유형인지 체크
        if (element.getKind() != CLASS) {
          continue;
        }

        SearchClass searchClass = element.getAnnotation(SearchClass.class);

        if (isNull(searchClass)) {
          continue;
        }

        if (!searchClass.base()) {
          continue;
        }

        TypeElement typeElement = (TypeElement) element;

        baseFieldList.addAll(getFieldList(typeElement));
      }

      for (Element element : roundEnv.getRootElements()) {
        if (element.getKind() != CLASS) {
          continue;
        }

        SearchClass searchClass = element.getAnnotation(SearchClass.class);

        if (isNull(searchClass)) {
          continue;
        }

        if (searchClass.base()) {
          continue;
        }

        TypeElement typeElement = (TypeElement) element;
        List<SearchCommentModel> fieldList = getFieldList(typeElement);

        fieldList.addAll(baseFieldList);

        List<SearchCommentModel> distinctFieldList = fieldList.stream()
            .filter(distinctByKey(SearchCommentModel::getField)).toList();

        /*
         * 필드 스펙 정의
         * TODO: 다른 방법이 있는지 체크
         */
        List<FieldSpec> fieldSpecList = distinctFieldList.stream()
            .map(item -> FieldSpec.builder(HashMap.class, item.getField())
                .addModifiers(PUBLIC, STATIC)
                .initializer(
                    "new HashMap<String, Object>() {{"
                        + "put(\"name\", \"" + item.getName() + "\");"
                        + "put(\"type\", \"" + item.getType() + "\");"
                        + (item.getListFlag()
                        ? "put(\"subType\", \"" + item.getSubType() + "\");"
                        : "")
                        + "put(\"format\", \"" + item.getFormat() + "\");"
                        + "put(\"comment\", \"" + item.getComment() + "\");"
                        + "}}"
                ).build()
            ).toList();

        ClassName className = ClassName.get(typeElement);
        TypeSpec typeSpec = TypeSpec
            .classBuilder("F" + className.simpleName())
            .addModifiers(PUBLIC)
            .addFields(fieldSpecList)
            .build();

        // 클래스 생성
        JavaFile
            .builder(className.packageName(), typeSpec)
            .build()
            .writeTo(processingEnv.getFiler());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return true;
  }

  /**
   * 필드 목록 조회
   *
   * @param typeElement the type element
   * @return the field list
   * @apiNote 필드 목록을 {@link SearchCommentModel} 로 만들어서 반환
   * @author FreshR
   * @since 2023. 1. 12. 오후 2:59:34
   */
  public List<SearchCommentModel> getFieldList(TypeElement typeElement) {
    // 멤버 요소 목록 조회
    List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
    List<SearchCommentModel> commentList = new ArrayList<>();

    for (Element field : enclosedElements) {
      // 필드 유형인지 체크
      if (field.getKind() != FIELD) {
        continue;
      }

      String fieldName = field.getSimpleName().toString();
      SearchComment searchComment = field.getAnnotation(SearchComment.class);
      DateTimeFormat dateTimeFormat = field.getAnnotation(DateTimeFormat.class);

      String comment = "";
      String format = "";

      if (!isNull(searchComment)) {
        comment = searchComment.value();
      }

      if (!isNull(dateTimeFormat)) {
        format = dateTimeFormat.pattern();
      }

      String type = field.asType().toString();
      String listType = List.class.getName();
      boolean listFlag = type.contains(listType);
      String subType = "";

      if (listFlag) {
        subType = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
      }

      // Generic ID 유형의 경우 분기 처리
      if (type.equals("ID")) {
        SearchCommentModelBuilder commentBuilder = SearchCommentModel
            .builder()
            .name(fieldName)
            .comment(comment)
            .format(format)
            .subType(subType)
            .listFlag(listFlag);

        commentList.add(commentBuilder.field(fieldName + "Long").type("java.lang.Long").build());
        commentList.add(commentBuilder.field(fieldName + "Integer").type("java.lang.Integer").build());
        commentList.add(commentBuilder.field(fieldName + "String").type("java.lang.String").build());
      } else {
        commentList.add(SearchCommentModel
            .builder()
            .field(fieldName)
            .name(fieldName)
            .comment(comment)
            .format(format)
            .type(type)
            .subType(subType)
            .listFlag(listFlag)
            .build());
      }
    }

    return commentList;
  }

  /**
   * 중복 필드를 제거
   *
   * @param <T>          the type parameter
   * @param keyExtractor the key extractor
   * @return the predicate
   * @apiNote 중복 필드를 제거하기 위한 조건 정의
   * @author FreshR
   * @since 2023. 1. 12. 오후 2:59:34
   */
  public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
    Map<Object, Boolean> map = new ConcurrentHashMap<>();

    return item -> map.putIfAbsent(keyExtractor.apply(item), Boolean.TRUE) == null;
  }

}

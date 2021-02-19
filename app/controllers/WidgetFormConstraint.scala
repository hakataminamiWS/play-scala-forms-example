package object controllers {

  import play.api.data.validation._

  val prefectureCheckConstraint: Constraint[String] =
    Constraint("都道府県チェック")({ value =>
      val errors = Prefecture.allPrefecture.contains(value) match {
        case false => Seq(ValidationError("error.prefecture"))
        case true  => Nil
      }
      if (errors.isEmpty) {
        Valid
      } else {
        Invalid(errors)
      }
    })

}

package unit

import controllers.WidgetForm
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.i18n._
import play.api.mvc._
import play.api.test._

/** Unit tests that do not require a running Play application.
  *
  * This is useful for testing forms and constraints.
  */
class UnitSpec extends PlaySpec {
  import play.api.data.FormBinding.Implicits._

  "WidgetForm" must {

    "apply successfully from request" in {
      // The easiest way to test a form is by passing it a fake request.
      val call = controllers.routes.WidgetController.createWidget()
      implicit val request: Request[_] = FakeRequest(call)
        .withFormUrlEncodedBody("prefecture" -> "GIFU", "price" -> "100")
      // A successful binding using an implicit request will give you a form with a value.
      val boundForm = WidgetForm.form.bindFromRequest()
      // You can then get the widget data out and test it.
      val widgetData = boundForm.value.get

      widgetData.prefecture must equal("GIFU")
      widgetData.price must equal(100)
    }

    "apply successfully from map" in {
      // You can also bind directy from a map, if you don't have a request handy.
      val data = Map("prefecture" -> "GIFU", "price" -> "100")
      // A successful binding will give you a form with a value.
      val boundForm = WidgetForm.form.bind(data)
      // You can then get the widget data out and test it.
      val widgetData = boundForm.value.get

      widgetData.prefecture must equal("GIFU")
      widgetData.price must equal(100)
    }

    "show errors when applied a negative price" in {
      // Pass in a negative price that fails the constraints...
      val data = Map("prefecture" -> "GIFU", "price" -> "-100")

      // ...and binding the form will show errors.
      val errorForm = WidgetForm.form.bind(data)
      // You can then get the widget data out and test it.
      val listOfErrors = errorForm.errors

      // Note that the FormError's key is the field it was bound to.
      // If there is no key, then it is a "global error".
      val formError: FormError = listOfErrors.head
      formError.key must equal("price")

      // In this case, we don't have any global errors -- they're caused
      // when a constraint on the form itself fails.
      errorForm.hasGlobalErrors mustBe false

      // The message is in the language that was "preferred" by the request's Messages
      // component.  The closest messages file i.e. messages.en is looked up, and then
      // the constraint key ("error.min") is looked up.  If there is no user defined
      // mapping, then the default messages file is "messages.default", which has
      //
      // error.min=Must be greater or equal to {0}
      //
      // but in this case, we haven't passed in a request, because we called bind instead of bindFromRequest!
      //
      // As such, when there is nothing in scope, the error message is the key itself.
      formError.message must equal("error.min")

      // You get the content of the message by calling Messages(key, args) with an in scope MessagesProvider.
      //
      // Usually you'll do this through dependency injection with
      // app.inject[MessagesApi] and messageApi.preferred(request), but we can
      // do it by hand here just to demonstrate what happens underneath the hood:
      val lang: Lang = Lang.defaultLang
      val messagesApi: MessagesApi = new DefaultMessagesApi(
        Map(lang.code -> Map("error.min" -> "Must be greater or equal to {0}"))
      )
      val messagesProvider: MessagesProvider = messagesApi.preferred(Seq(lang))
      val message: String =
        Messages(formError.message, formError.args: _*)(messagesProvider)

      // And the message will be run through with the arguments:
      message must equal("Must be greater or equal to 0")
    }

    "show errors when applied a non-exist key at prefecture field" in {
      // Pass in a non-exist key at prefecure field that fails the constraints...
      val call = controllers.routes.WidgetController.createWidget()
      implicit val request: Request[_] = FakeRequest(call)
        .withFormUrlEncodedBody("prefecture" -> "XXX", "price" -> "100")

      val errorForm = WidgetForm.form.bindFromRequest()

      val listOfErrors = errorForm.errors

      val formError: FormError = listOfErrors.head
      formError.key must equal("prefecture")
      errorForm.hasGlobalErrors mustBe false
      formError.message must equal("error.prefecture")
    }

    "show errors when applied a non-number value" in {
      val data = Map("prefecture" -> "GIFU", "price" -> "abc")

      val errorForm = WidgetForm.form.bind(data)
      val listOfErrors = errorForm.errors

      val formError: FormError = listOfErrors.head
      formError.key must equal("price")

      errorForm.hasGlobalErrors mustBe false

      formError.message must equal("error.number")
    }
  }
}

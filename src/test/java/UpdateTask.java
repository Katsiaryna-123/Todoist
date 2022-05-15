import Helpers.Helper;
import Helpers.Token;
import io.restassured.RestAssured;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.hamcrest.Matchers.equalTo;

public class UpdateTask {
    String token;
    Helper helper;

    @BeforeClass
    void generateToken() {
        token = new Token().getToken();
        helper = new Helper();
    }
    @AfterMethod
    public void afterTest() {
        helper.deleteEverything();
    }

    @Test
    void statusCodeWhenUpdateContentOfTask() {
        long taskId = helper.createBasicTask(token);

        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "updated name of the task")
                .post("https://api.todoist.com/rest/v1/tasks/" + taskId)
                .then()
                .statusCode(204);
    }

    @Test
    void updateContentInInbox() {
        long taskId = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "Buy twitter")
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "buy Belarus to show from where the attack was planned")
                .post("https://api.todoist.com/rest/v1/tasks/" + taskId);

        RestAssured.given()
                .auth().oauth2(token)
                .get("https://api.todoist.com/rest/v1/tasks/" + taskId)
                .then()
                .body("content", equalTo("buy Belarus to show from where the attack was planned"));

    }

    @Test
    void updateDescriptionInProject() {
        long projectId = helper.createProject();

        long taskId = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("project_id", projectId)
                .queryParam("content", "Buy tickets")
                .queryParam("description", "Invite someone")
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        RestAssured.given().auth().oauth2(token)
                .queryParam("description", "Invite Mihkel and Marilis")
                .post("https://api.todoist.com/rest/v1/tasks/" + taskId);

        RestAssured.given().auth().oauth2(token)
                .get("https://api.todoist.com/rest/v1/tasks/" + taskId)
                .then()
                .body("description", equalTo("Invite Mihkel and Marilis"));
    }

    @Test
    void updatePriorityInSection() {
        long projectId = helper.createProject();
        long sectionId = helper.createSection(projectId);

        long taskId = RestAssured.given().auth().oauth2(token)
                .queryParam("projectId", projectId)
                .queryParam("section_id", sectionId)
                .queryParam("content", "Buy tickets")
                .queryParam("priority", 1)
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        RestAssured.given().auth().oauth2(token)
                .queryParam("priority", 4)
                .post("https://api.todoist.com/rest/v1/tasks/" + taskId);

        RestAssured.given().auth().oauth2(token)
                .get("https://api.todoist.com/rest/v1/tasks/" + taskId)
                .then()
                .body("priority", equalTo(4));
    }

    @Test
    void updateSeveralParameters() {
        long projectId = helper.createProject();
        long sectionId = helper.createSection(projectId);

        long taskId = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("projectId", projectId)
                .queryParam("section_id", sectionId)
                .queryParam("content", "купить морковку")
                .queryParam("priority", 3)
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("priority", 3)
                .queryParam("content", "buy carrot")
                .queryParam("description", "")
                .post("https://api.todoist.com/rest/v1/tasks/" + taskId);

        RestAssured.given()
                .auth().oauth2(token)
                .get("https://api.todoist.com/rest/v1/tasks/" + taskId)
                .then()
                .assertThat()
                .body("description", equalTo(""))
                .body("content", equalTo("buy carrot"))
                .body("priority", equalTo(3));
    }

    @Test
    void notUpdatedFieldsRemainTheSameAfterOneFieldUpdate() {
        long taskId = helper.createBasicTask(token);

        String createdTaskResponse = RestAssured.given()
                .auth().oauth2(token)
                .get("https://api.todoist.com/rest/v1/tasks/" + taskId)
                .body()
                .asString();

        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "call family")
                .post("https://api.todoist.com/rest/v1/tasks/" + taskId);

        String updatedTaskResponse = RestAssured.given()
                .auth().oauth2(token)
                .get("https://api.todoist.com/rest/v1/tasks/" + taskId)
                .body()
                .asString();

        assertThatJson(createdTaskResponse).whenIgnoringPaths("content").isEqualTo(updatedTaskResponse);
    }
}

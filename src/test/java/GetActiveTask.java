import Helpers.Helper;
import Helpers.Token;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class GetActiveTask {
    String token;
    Helper helper;

    @BeforeClass
    void generateToken() {
        token = new Token().getToken();
        helper = new Helper();
    }

    @AfterMethod
    void remove() {
        helper.deleteEverything();
    }

    @Test
    void statusCode() {
        helper.createBasicTask(token);

        RestAssured.given()
                .auth().oauth2(token)
                .get(" https://api.todoist.com/rest/v1/tasks")
                .then()
                .statusCode(200);
    }

    @Test
    void contentType() {
        long taskId = helper.createBasicTask(token);

        Response response = RestAssured.given()
                .auth().oauth2(token)
                .get(" https://api.todoist.com/rest/v1/tasks/" + taskId);
        String contentType = response.header("Content-Type");

        Assert.assertEquals(contentType, "application/json");
    }

    @Test
    void rightTaskIdInUrl() {
        long taskId = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "Buy orange")
                .when()
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        RestAssured.given()
                .auth().oauth2(token)
                .get("https://api.todoist.com/rest/v1/tasks/" + taskId)
                .then()
                .assertThat()
                .body("url", equalTo("https://todoist.com/showTask?id=" + taskId));
    }

    @Test
    void oneActiveTaskInTheProjectWithClosedTasks() {
        long projectIdWithCompletedTasksAndOneActive = helper.createProject();

        helper.createCompletedTasks(projectIdWithCompletedTasksAndOneActive, token, 10);
        List<Long> taskIds = helper.createActiveTasks(projectIdWithCompletedTasksAndOneActive, token, 1);
        List<Long> response = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("completed", false)
                .queryParam("project_id", projectIdWithCompletedTasksAndOneActive)
                .get("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        Assert.assertEquals(taskIds, response);
    }

    @Test
    void gettingActiveTaskFromSection() {
        long projectId = helper.createProject();
        long sectionId = helper.createSection(projectId);

        long createdTaskId = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("section_id", sectionId)
                .queryParam("content", "Buy notebook")
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");
        List<Long> responseTaskId = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("section_id", sectionId)
                .get("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        Assert.assertTrue(responseTaskId.contains(createdTaskId));
        Assert.assertEquals(responseTaskId.size(), 1);
    }
}


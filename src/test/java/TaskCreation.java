import Helpers.Helper;
import Helpers.Token;
import JavaBeans.CreateTaskRequestTemplate;
import JavaBeans.CreateTaskResponseTemplate;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.restassured.RestAssured;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;


public class TaskCreation {
    String token;
    Helper helper;

    @BeforeClass
    void generateToken() {
        token = new Token().getToken();
        helper = new Helper();
    }
    @AfterMethod
    void remove() {
        new Helper().deleteEverything();
    }

    @Test
    void statusCodeCheck() {
        RestAssured.given()
                .auth().oauth2(token)
                .given()
                .queryParam("content", "Buy Milk")
                .queryParam("due_string", "tomorrow at 12:00")
                .queryParam("due_lang", "en")
                .queryParam("priority", 4)
                .queryParam("completed", equalTo(false))
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .statusCode(200);
    }

    @Test
    void contentTypeCheck() {
        RestAssured.given()
                .auth().oauth2(token).
                given()
                .queryParam("content", "jäätist osta")
                .queryParam("due_string", "tomorrow at 12:00")
                .queryParam("priority", 4)
                .when()
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .contentType("application/json");
    }

    @Test
    void verifyCreatedTaskAllFields() {
        long projectId = helper.createProject();
        long sectionId = helper.createSection(projectId);

        CreateTaskRequestTemplate task = new CreateTaskRequestTemplate();
        task.setContent("create project");
        task.setDescription("no decription");
        task.setPriority(4);
        task.setOrder(3);
        task.setProject_id(projectId);
        task.setSection_id(sectionId);
        task.setDue_string("tomorrow");
        long id = helper.createBasicTask(token, task);

        CreateTaskResponseTemplate result = RestAssured.given()
                .auth().oauth2(token)
                .get(" https://api.todoist.com/rest/v1/tasks/" + id)
                .as(CreateTaskResponseTemplate.class);

        Assert.assertEquals(result.getId(), id);
        Assert.assertEquals(result.getContent(), task.getContent());
        Assert.assertEquals(result.getDescription(), task.getDescription());
        Assert.assertEquals(result.getPriority(), task.getPriority());
        Assert.assertEquals(result.getOrder(), task.getOrder());
        Assert.assertEquals(result.getProject_id(), task.getProject_id());
        Assert.assertEquals(result.getSection_id(), task.getSection_id());
        Assert.assertEquals(result.getDue().getString(), task.getDue_string());
    }

    @Test
    void createManyTasksInTheProject() {
        int numberOfTasks = 100;
        long projectId = helper.createProject();

        List<Long> taskIds = helper.createActiveTasks(projectId, token, numberOfTasks);
        List<Long> tasksInTheProject = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("project_id", projectId)
                .get("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");
        Assert.assertEquals(taskIds, tasksInTheProject);
    }

    @Test
    void checkDateTransformation() {
        RestAssured.given()
                .auth().oauth2(token)
                .given()
                .queryParam("content", "Buy gift for mum ")
                .queryParam("due_string", "May 18 2022")
                .when()
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .assertThat()
                .body("due.date", equalTo("2022-05-18"));
    }

    @Test
    void checkRecurringDate() {
        RestAssured.given()
                .auth().oauth2(token)
                .given()
                .queryParam("content", "Buy gift for mum ")
                .queryParam("due_string", "Every Monday")
                .when()
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .assertThat()
                .body("due.recurring", equalTo(true));
    }

    @Test
    void taskWithoutMandatoryField() {
        RestAssured.given()
                .auth().oauth2(token)
                .given()
                .queryParam("due_string", "tomorrow at 13:00")
                .queryParam("due_lang", "en")
                .queryParam("priority", 1)
                .when()
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .statusCode(400);
    }

    @Test
    void taskOnlyWithMandatoryField() {
        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "Buy Flowers")
                .when()
                .post("https://api.todoist.com/rest/v1/tasks").then().statusCode(200);
    }

    @Test
    void taskWithParent() {
        long parentTaskId = helper.createBasicTask(token);

        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "play badminton")
                .queryParam("parent_id", parentTaskId)
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .assertThat()
                .body("parent_id", equalTo(parentTaskId));
    }

    @Test
    void taskWithProjectId() {
        Long projectId = helper.createProject();

        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "Buy Cheese")
                .queryParam("project_id", projectId)
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .body("project_id", equalTo(projectId));
    }

    @Test
    void taskWithoutProjectId() {
        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "Buy lemon")
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .body("project_id", equalTo(helper.getInboxId()));
    }

    @Test
    void priorityOutsideBounds() {
        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "Buy piim")
                .queryParam("priority", "400")
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .statusCode(400);
    }

    @Test
    void possibilityToCreateTaskWhenSomeAlreadyExist() {
        long projectId = helper.createProject();
        helper.createActiveTasks(projectId, token, 3);

        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "Buy Cheese")
                .queryParam("project_id", projectId)
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .body("project_id", equalTo(projectId));
    }

    @Test
    void possibilityToCreateTaskInSection() {
        long projectId = helper.createProject();
        int sectionId = helper.createSection(projectId);
        long createdInFirstSectionTasks =
                RestAssured.given()
                        .auth().oauth2(token)
                        .queryParam("section_id", sectionId)
                        .queryParam("content", "Buy phone")
                        .post("https://api.todoist.com/rest/v1/tasks")
                        .then()
                        .extract()
                        .response()
                        .path("id");

        List<Long> tasksIdInFirstSection = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("section_id", sectionId)
                .get("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");
        Assert.assertEquals(tasksIdInFirstSection.size(), 1);
        Assert.assertTrue(tasksIdInFirstSection.contains(createdInFirstSectionTasks));
    }
}
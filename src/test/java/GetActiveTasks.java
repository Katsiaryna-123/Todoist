import Helpers.Helper;
import Helpers.Token;
import JavaBeans.CreateTaskRequestTemplate;
import JavaBeans.GetAllTasksResponseTemplate;
import io.restassured.RestAssured;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

import static org.hamcrest.Matchers.*;


public class GetActiveTasks {
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
    void statusCode() {
        helper.createActiveTasks(helper.getInboxId(), token, 10);
        RestAssured.given()
                .auth().oauth2(token)
                .get(" https://api.todoist.com/rest/v1/tasks")
                .then()
                .statusCode(200);
    }

    @Test
    void verifyAllTasksAllFields() {
        CreateTaskRequestTemplate task1 = new CreateTaskRequestTemplate();
        task1.setContent("t1");
        task1.setDescription("d1");
        task1.setPriority(1);
        task1.setOrder(1);

        CreateTaskRequestTemplate task2 = new CreateTaskRequestTemplate();
        task2.setContent("t2");
        task2.setDescription("d2");
        task2.setPriority(2);
        task2.setOrder(2);

        CreateTaskRequestTemplate task3 = new CreateTaskRequestTemplate();
        task3.setContent("t3");
        task3.setDescription("d3");
        task3.setPriority(3);
        task3.setOrder(3);
        CreateTaskRequestTemplate[] initData = new CreateTaskRequestTemplate[]{task1, task2, task3};

        helper.createActiveTasks(token, initData);//data creation

        GetAllTasksResponseTemplate[] resultData = RestAssured.given()
                .auth().oauth2(token)
                .get(" https://api.todoist.com/rest/v1/tasks")
                .as(GetAllTasksResponseTemplate[].class);

        Assert.assertEquals(resultData.length, initData.length);
        for (int i = 0; i < initData.length; i++) {
            Assert.assertEquals(resultData[i].getContent(), initData[i].getContent());
            Assert.assertEquals(resultData[i].getDescription(), initData[i].getDescription());
            Assert.assertEquals(resultData[i].getPriority(), initData[i].getPriority());
            Assert.assertEquals(resultData[i].getOrder(), initData[i].getOrder());
            Assert.assertNull(resultData[i].getAssignee());
            Assert.assertEquals(resultData[i].getAssigner(), 0);
            Assert.assertEquals(resultData[i].getComment_count(), 0, "should be zero comments");
            Assert.assertNull(resultData[i].getParent_id());
            Assert.assertEquals(resultData[i].getSection_id(), 0);
        }
    }

    @Test
    void tasksNumberInResponseIsTheSameAsNumberOfTasksCreated() {
        int numberOfTasks = 10;
        long projectId = helper.createProject();

        helper.createActiveTasks(projectId, token, numberOfTasks);

        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("project_id", projectId)
                .get("https://api.todoist.com/rest/v1/tasks")
                .then()
                .body("size()", equalTo(numberOfTasks));
    }

    @Test
    void mandatoryFieldIsPresent() {
        helper.createActiveTasks(helper.getInboxId(), token, 2);
        GetAllTasksResponseTemplate[] allTasks = RestAssured.given()
                .auth().oauth2(token)
                .get(" https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .body()
                .as(GetAllTasksResponseTemplate[].class);

        Assert.assertEquals(allTasks.length, 2);
        for (GetAllTasksResponseTemplate allTask : allTasks) {
            Assert.assertNotNull(allTask.getContent());
        }
    }

    @Test
    void filterTasksByProjectId() {
        long projectId = helper.createProject();

        helper.createActiveTasks(helper.getInboxId(), token, 10);
        List<Long> expectedTasksIds = helper.createActiveTasks(projectId, token, 3);
        List<Long> createdListOfTasks = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("project_id", projectId)
                .get(" https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        Assert.assertEquals(expectedTasksIds, createdListOfTasks);
    }

    @Test
    void filterTasksByDueToday() {//due date шалит
        long projectId = helper.createProject();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        helper.createActiveTasks(projectId, token, 2);
        long taskId = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "Name_Random")
                .queryParam("due_date", LocalDate.now().format(dtf))
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        List<Long> createdListOfTasks = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("filter", "today")
                .get(" https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        Assert.assertEquals(createdListOfTasks.size(), 1);
        Assert.assertEquals(taskId, createdListOfTasks.get(0));
    }

    @Test
    void tryToGetActiveTasksFromProjectWhereAllTasksAreClosed() {
        long projectIdWithActiveTasks = helper.createProject();
        long projectIdWithCompletedTasks = helper.createProject();

        helper.createActiveTasks(projectIdWithActiveTasks, token, 3);
        helper.createCompletedTasks(projectIdWithCompletedTasks, token, 2);
        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("completed", false)
                .queryParam("project_id", projectIdWithCompletedTasks)
                .get(" https://api.todoist.com/rest/v1/tasks/")
                .then()
                .body("size()", equalTo(0));
    }

    @Test
    void closeTasksAndCheckThatTasksQuantityDecreasedByNumberOfClosedTasks() {
        long projectIdWithActiveAndCompletedTasks = helper.createProject();

        List<Long> taskIds = helper.createActiveTasks(projectIdWithActiveAndCompletedTasks, token, 10);
        helper.closeTasks(taskIds.subList(0, 9), token);
        RestAssured.given()
                .auth().oauth2(token)
                .queryParam("completed", false)
                .queryParam("project_id", projectIdWithActiveAndCompletedTasks)
                .get(" https://api.todoist.com/rest/v1/tasks/")
                .then()
                .body("size()", equalTo(1));
    }

    @Test
    void getTasksFromInboxWhenTasksArePresentBothInInboxAndInAnotherProject() {
        long projectWithTasks = helper.createProject();

        List<Long> inboxTasksIds = helper.createActiveTasks(helper.getInboxId(), token, 6);
        helper.createActiveTasks(projectWithTasks, token, 3);
        List<Long> list = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("project_id", helper.getInboxId())
                .get("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        Assert.assertEquals(list, inboxTasksIds);
    }

    @Test
    void getTasksFromTheProjectWhenTasksArePresentBothInInboxAndInTheProject() {
        long projectWithTasks = helper.createProject();

        helper.createActiveTasks(helper.getInboxId(), token, 2);
        List<Long> projectTasksIds = helper.createActiveTasks(projectWithTasks, token, 3);
        List<Long> list = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("project_id", projectWithTasks)
                .get("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");

        Assert.assertEquals(list, projectTasksIds);
    }
}

package Helpers;

import JavaBeans.CreateTaskRequestTemplate;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

public class Helper {

    public long getInboxId() {
        Token token = new Token();
        List<Long> projectIds = RestAssured.given().auth().oauth2(token.getToken())
                .queryParam("inbox_project", true)
                .get(" https://api.todoist.com/rest/v1/projects")
                .then()
                .extract()
                .path("id");

        return projectIds.get(0);
    }

    public long createProject() {
        Token token = new Token();
        long projectId = RestAssured.given().auth().oauth2(token.getToken()).
                given().
                queryParam("name", "many Items").
                when().
                post("https://api.todoist.com/rest/v1/projects")
                .then().statusCode(200)
                .extract().path("id");
        return projectId;
    }

    public List<Long> createActiveTasks(Long projectId, String token, int numberOfTasksToCreate) {
        List<Long> taskIds = new ArrayList<>();
        for (int i = 0; i < numberOfTasksToCreate; i++) {
            Response response = RestAssured.given()
                    .auth().oauth2(token)
                    .queryParam("content", "Name_" + i)
                    .queryParam("project_id", projectId)
                    .expect()
                    .body("project_id", equalTo(projectId))
                    .when()
                    .post("https://api.todoist.com/rest/v1/tasks");

            taskIds.add(response
                    .then()
                    .extract()
                    .body()
                    .path("id"));

        }
        return taskIds;
    }

    public void createActiveTasks(String token, CreateTaskRequestTemplate[] initData) {
        for (CreateTaskRequestTemplate initDatum : initData) {
            RestAssured.given().auth().oauth2(token)
                    .contentType("application/json")
                    .body(initDatum)
                    .post("https://api.todoist.com/rest/v1/tasks")
                    .then().statusCode(200);
        }
    }

    public void createCompletedTasks(Long projectId, String token, int numberOfTasksToCreate) {
        List<Long> taskIds = createActiveTasks(projectId, token, numberOfTasksToCreate);
        for (Long taskId : taskIds) {
            RestAssured.given().auth().oauth2(token)
                    .post("https://api.todoist.com/rest/v1/tasks/" + taskId + "/close");
        }

    }

    public void closeTasks(List<Long> taskIds, String token) {
        for (Long taskId : taskIds) {
            RestAssured.given().auth().oauth2(token).post("https://api.todoist.com/rest/v1/tasks/" + taskId + "/close")
                    .then()
                    .statusCode(204);

        }
    }

    public long createBasicTask(String token) {
        long TaskId = RestAssured.given()
                .auth().oauth2(token)
                .queryParam("content", "Name_Random")
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .extract()
                .path("id");
        return TaskId;

    }

      public long createBasicTask(String token, CreateTaskRequestTemplate createdTask) {
        long taskId = RestAssured.given()
                .auth().oauth2(token)
                .contentType("application/json")
                .body(createdTask)
                .post("https://api.todoist.com/rest/v1/tasks")
                .then()
                .statusCode(200)
                .extract()
                .path("id");
        return taskId;

    }

    public void deleteEverything() {
        Token token = new Token();
        String tokenStr = token.getToken();
        Response tasksResponse = RestAssured.given().auth().oauth2(tokenStr).
                get("https://api.todoist.com/rest/v1/tasks");
        List<Long> taskIds = tasksResponse.jsonPath().getList("id");
        for (Long taskId : taskIds) {
            RestAssured.given().auth().oauth2(tokenStr)
                    .delete("https://api.todoist.com/rest/v1/tasks/" + taskId);

        }
        Response projectResponse = RestAssured.given().auth().oauth2(tokenStr).
                get("https://api.todoist.com/rest/v1/projects");
        List<Long> projectsIds = projectResponse.jsonPath().getList("id");
        for (Long projectsId : projectsIds) {
            RestAssured.given().auth().oauth2(tokenStr)
                    .delete("https://api.todoist.com/rest/v1/projects/" + projectsId);

        }

    }

    public int createSection(long projectId) {
        JSONObject requestParams = new JSONObject();
        requestParams.put("name", "test");
        requestParams.put("project_id", projectId);

        int sectionId = RestAssured.given().
                header("Authorization", "Bearer 7a223d0685fe8c6e10eb51e2f0889d8ab084c744").
                contentType("application/json").
                body(requestParams.toString()).
                when().
                post("https://api.todoist.com/rest/v1/sections")
                .then().statusCode(200)
                .extract()
                .path("id");

        return sectionId;
    }
}

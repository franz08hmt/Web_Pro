package vn.edu.webpro.robotlab.model;

import vn.edu.webpro.robotlab.web.Json;

public record AssemblySession(long id,long userId,String robotId,String status) {
    public String toJson(){return "{\"id\":"+Json.quote(Long.toString(id))+",\"userId\":"+Json.quote(Long.toString(userId))+",\"robotId\":"+Json.quote(robotId)+",\"status\":"+Json.quote(status)+",\"progressPercent\":0,\"components\":[],\"steps\":[],\"assembledPartIds\":[]}";}
}

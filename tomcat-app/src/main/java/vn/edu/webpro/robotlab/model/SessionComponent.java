package vn.edu.webpro.robotlab.model;
import vn.edu.webpro.robotlab.web.Json;
public record SessionComponent(String componentId,boolean isPrepared){public String toJson(){return "{\"componentId\":"+Json.quote(componentId)+",\"isPrepared\":"+isPrepared+"}";}}

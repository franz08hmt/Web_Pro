package vn.edu.webpro.robotlab.model;

import java.util.List;
import vn.edu.webpro.robotlab.web.Json;

public record AssemblySession(long id,long userId,String robotId,String status,List<SessionComponent> components,List<SessionStep> steps,List<String> assembledPartIds,int requiredComponentCount) {
    public String toJson(){String parts=components.stream().map(SessionComponent::toJson).reduce((a,b)->a+","+b).orElse("");String stepJson=steps.stream().map(SessionStep::toJson).reduce((a,b)->a+","+b).orElse("");String assembled=assembledPartIds.stream().map(Json::quote).reduce((a,b)->a+","+b).orElse("");long prepared=components.stream().filter(SessionComponent::isPrepared).map(SessionComponent::componentId).distinct().count();int progress=requiredComponentCount==0?0:(int)(prepared*100/requiredComponentCount);return "{\"id\":"+Json.quote(Long.toString(id))+",\"userId\":"+Json.quote(Long.toString(userId))+",\"robotId\":"+Json.quote(robotId)+",\"status\":"+Json.quote(status)+",\"progressPercent\":"+progress+",\"components\":["+parts+"],\"steps\":["+stepJson+"],\"assembledPartIds\":["+assembled+"]}";}
}

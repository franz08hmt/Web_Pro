'use strict';
const test=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const root=path.resolve(__dirname,'../..');
const files=['controller/AdminQuizServlet.java','controller/AdminStatsServlet.java','controller/OrderServlet.java',
  'util/JsonUtil.java','business/ShopOrder.java','business/Robot.java','business/LibraryResource.java','controller/QuizServlet.java'];
function javaCode(text){return text.replace(/"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\/\/[^\n]*|\/\*[\s\S]*?\*\//g,' ');}
test('final hardening Java changes use only course syntax and bounded line lengths',()=>{
  for(const file of files){const text=fs.readFileSync(path.join(root,'tomcat-app/src/main/java/vn/edu/webpro/robotlab',file),'utf8');
    assert.doesNotMatch(javaCode(text),/\.stream\s*\(|->|::|\bswitch\s*\(|\bOptional\b|\bvar\s|\b(?:List|Map|Set)\.of\s*\(|java\.time/);
    for(const [index,line]of text.split(/\r?\n/).entries())assert.ok([...line].length<=120,file+':'+(index+1));
  }
});
test('admin session total uses the tested JavaBean rather than Servlet aggregation',()=>{
  const text=fs.readFileSync(path.join(root,'tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/AdminStatsServlet.java'),'utf8');
  assert.match(text,/summary\.summarizeSessions\(sessionCounts\)/);assert.match(text,/summary\.getTotalSessions\(\)/);
});
test('attempt ID overflow is caught only around parsing and leaves database errors separate',()=>{
  const text=fs.readFileSync(path.join(root,'tomcat-app/src/main/java/vn/edu/webpro/robotlab/controller/QuizServlet.java'),'utf8');
  assert.match(text,/try\s*\{\s*attemptId = Long\.parseLong\(rawId\);\s*\}\s*catch \(NumberFormatException e\)/);
  assert.match(text,/QuizAttemptDB\.selectAttempt\(attemptId, user\.getId\(\)\)/);
  assert.match(text,/catch \(SQLException e\)\s*\{\s*ResponseUtil\.sendDatabaseUnavailable/);
  assert.match(text,/"\/attempts"\.equals\(path\) \|\| "\/attempts\/"\.equals\(path\)/);
});

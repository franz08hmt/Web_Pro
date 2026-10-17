'use strict';
const fs=require('node:fs'),path=require('node:path'),vm=require('node:vm');
const test=require('node:test'),assert=require('node:assert/strict');
const root=path.resolve(__dirname,'../..');
function source(file){return fs.readFileSync(path.join(root,file),'utf8');}
function body(file,start,end){const text=source(file);return text.slice(text.indexOf(start),text.indexOf(end,text.indexOf(start)));}
function preparation({id=null,error=null,available=true}={}){
  const calls=[],notices=[],input={disabled:false},button={disabled:false};
  const session={id:21,robotId:'line-follower',assemblyMode:'DETAILED',status:'PREPARING'};
  const api={get:async id=>{calls.push(['get',id]);return {...session,assemblyMode:'GROUP'};},
    createDetailed:async model=>{calls.push(['detailed',model]);if(error)throw error;return session;},
    createOrResume:async model=>{calls.push(['legacy',model]);if(error)throw error;return {...session,assemblyMode:'GROUP'};}};
  const context={window:{dispatchEvent(){}},CustomEvent:class{},Boolean,Set,URL,
    requestedSessionId:id,selectionRevision:0,activeSession:null,sessionApi:available?api:null,
    checklist:{querySelectorAll:()=>[input]},resetButton:button,start3DButton:{hidden:false},modelSelect:{value:'line-follower'},
    status:{textContent:''},applySession(session){context.activeSession=session;},updateProgress(_,notice){notices.push(notice);}};
  vm.createContext(context);vm.runInContext(body('assets/js/main.js','  async function restoreSession()', '\n  checklist.addEventListener'),context);
  return {context,calls,notices,input,button,run:()=>context.restoreSession()};
}
test('normal preparation opens detailed mode even if legacy resume would return GROUP',async()=>{
  const p=preparation();await p.run();assert.deepEqual(p.calls,[['detailed','line-follower']]);
  assert.equal(p.context.activeSession.assemblyMode,'DETAILED');
});
test('explicit GROUP preparation URL reads the chosen ID without silently replacing it',async()=>{
  const p=preparation({id:'17'});await p.run();assert.deepEqual(p.calls,[['get','17']]);
  assert.equal(p.context.activeSession.assemblyMode,'GROUP');
});
for(const status of [422,503,0])test('preparation fails closed for server/network error '+status,async()=>{
  const p=preparation({error:{status,code:status?'VALIDATION_ERROR':'NETWORK_ERROR',message:'Yêu cầu chưa thực hiện được.'}});
  await p.run();assert.equal(p.input.disabled,true);assert.equal(p.context.start3DButton.hidden,true);
  assert.doesNotMatch(p.notices.join(' '),/đang dùng dữ liệu trên trình duyệt|Tiến độ đã lưu/);
});
test('missing preparation API does not advertise account synchronization or enable editing',async()=>{
  const p=preparation({available:false});await p.run();assert.equal(p.input.disabled,true);
  assert.equal(p.context.start3DButton.hidden,true);
});
test('guest preview remains explicit after authentication is actually rejected',async()=>{
  const p=preparation({error:{code:'AUTH_REQUIRED',status:401,message:'Đăng nhập.'}});await p.run();
  assert.match(p.notices.join(' '),/Đăng nhập/);assert.equal(p.context.activeSession,null);
});
function room({id=null,error=null,status='IN_PROGRESS'}={}){
  const calls=[],sync=[];const context={window:{THREE:{},createAssemblyPart(){},dispatchEvent(){},
    location:{href:'http://localhost:8080/pages/lap-rap-3d.html?model=mini-arm',replace:url=>calls.push(['redirect',url])}},
    CustomEvent:class{},URL,Set,replayMode:false,requestedSessionId:id,activeSession:null,detailedRequested:false,model:{id:'mini-arm'},
    visualStorageKey:'local-preview',contentWarning:'',readStoredIds:()=>new Set(),restoreVisualAssembly(){},
    setPartInputsDisabled:value=>calls.push(['disabled',value]),setSyncStatus:(text,state)=>sync.push({text,state}),
    updateCompleteAvailability(){},showCompletionResult(){},restoreDetailed:async()=>calls.push(['layout']),
    sessionApi:{get:async()=>({id:17,robotId:'mini-arm',assemblyMode:'GROUP',status:'IN_PROGRESS'}),
      createDetailed:async()=>{calls.push(['detailed']);if(error)throw error;return {id:22,robotId:'mini-arm',assemblyMode:'DETAILED',status};},
      createOrResume:async()=>{calls.push(['legacy']);if(error)throw error;return {id:17,robotId:'mini-arm',assemblyMode:'GROUP',status};}}};
  vm.createContext(context);vm.runInContext(body('assets/js/assembly-3d.js','  async function restoreSession()', '\n  if (!window.THREE'),context);
  return {context,calls,sync,run:()=>context.restoreSession()};
}
test('3D without explicit session starts detailed mode rather than legacy resume',async()=>{
  const r=room();await r.run();assert.ok(r.calls.some(c=>c[0]==='detailed'));
  assert.ok(r.calls.some(c=>c[0]==='layout'));assert.ok(!r.calls.some(c=>c[0]==='legacy'));
});
test('direct 3D entry with an unprepared detailed kit leads to its own preparation page',async()=>{
  const r=room({status:'PREPARING'});await r.run();const redirect=r.calls.find(c=>c[0]==='redirect');
  assert.ok(redirect);const url=new URL(redirect[1]);assert.equal(url.pathname,'/pages/lap-rap.html');
  assert.equal(url.searchParams.get('model'),'mini-arm');assert.equal(url.searchParams.get('session'),'22');
});
test('explicit GROUP 3D URL remains GROUP',async()=>{
  const r=room({id:'17'});await r.run();assert.equal(r.context.activeSession.assemblyMode,'GROUP');
  assert.ok(!r.calls.some(c=>c[0]==='detailed'));
});
for(const status of [422,503,0])test('3D does not fall back to local progress after server/network failure '+status,async()=>{
  const r=room({error:{status,code:status?'VALIDATION_ERROR':'NETWORK_ERROR',message:'Yêu cầu chưa thực hiện được.'}});
  await r.run();assert.equal(r.sync.at(-1).state,'error');assert.ok(!r.calls.some(c=>c[0]==='disabled'&&c[1]===false));
});

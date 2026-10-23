'use strict';
const fs=require('node:fs'),path=require('node:path'),vm=require('node:vm');
const assert=require('node:assert/strict'),test=require('node:test');
const THREE=require('../../assets/vendor/three/three.min.js');
const source=fs.readFileSync(path.resolve(__dirname,'../../assets/js/assembly-3d-placement.js'),'utf8');
function dragAt(slot, options={}){
  const slots=options.slots||[
    {id:1,x:0,y:.1,z:0,label:'Nền',componentId:'frame'},
    {id:2,x:0,y:2.5,z:0,label:'Servo phía trên',componentId:'servo'}
  ];
  const root=new THREE.Group(),camera=new THREE.PerspectiveCamera(42,1,.1,100);
  camera.position.set(8,6,10);camera.lookAt(0,1,0);camera.updateMatrixWorld();
  const markers=new Map();for(const item of slots){const marker=new THREE.Object3D();marker.position.set(item.x,item.y+.23,item.z);
    root.add(marker);markers.set(String(item.id),marker);}
  root.updateMatrixWorld(true);const target=new THREE.Vector3(slot.x,slot.y+.23,slot.z).project(camera);
  const raycaster=new THREE.Raycaster(),pointer=new THREE.Vector2();
  const context={THREE,Math,root,camera,markers,options:{matchComponentSlots:Boolean(options.componentId)},
    layout:{instances:options.componentId
    ?[{id:'22',componentId:options.componentId}]:[],placements:options.placements||[],spec:{slots}},
    selectedInstance:options.componentId?'22':null,selectedSlot:null,
    canvas:{getBoundingClientRect:()=>({left:0,top:0,width:512,height:512})},
    drag:{object:new THREE.Object3D(),pointerId:1},plane:new THREE.Plane(new THREE.Vector3(0,1,0),-.9),
    point:new THREE.Vector3(),raycaster,previewOrientation(){},highlight(){},message(){},
    alignOrientation(){},renderDirections(){},updateRotationControls(){},selectSlot(){},ray(event){
      pointer.set(event.clientX/256-1,1-event.clientY/256);raycaster.setFromCamera(pointer,camera);
    }};
  context.selectSlot=slotId=>{context.selectedSlot=String(slotId);};
  vm.createContext(context);
  const helperStart=source.indexOf('  function findPreviewSlot(');
  const moveStart=source.indexOf('  function pointerMove('),upStart=source.indexOf('  function pointerUp(');
  vm.runInContext((helperStart>=0?source.slice(helperStart,moveStart):'')+source.slice(moveStart,upStart),context);
  context.pointerMove({pointerId:1,clientX:(target.x+1)*256,clientY:(1-target.y)*256});
  return context.selectedSlot;
}
test('drop targets distinguish vertically stacked mounts with the same x/z',()=>{
  assert.equal(dragAt({x:0,y:2.5,z:0}),'2');
});
test('the base mount remains targetable on screen',()=>{
  assert.equal(dragAt({x:0,y:.1,z:0}),'1');
});
test('dragging a servo near the occupied arm base previews its compatible servo mount',()=>{
  const slots=[
    {id:1,x:0,y:.12,z:0,label:'Bộ khung cánh tay',componentId:'arm-frame'},
    {id:2,x:0,y:.34,z:0,label:'Động cơ servo 1',componentId:'sg90'}
  ];
  assert.equal(dragAt({x:0,y:.12,z:0},{componentId:'sg90',slots,
    placements:[{instanceId:'11',slotId:'1'}]}),'2');
});
test('clicking near the occupied arm base selects the nearest compatible servo mount',()=>{
  const slots=[
    {id:1,x:0,y:.12,z:0,label:'Bộ khung cánh tay',componentId:'arm-frame'},
    {id:2,x:0,y:.34,z:0,label:'Động cơ servo 1',componentId:'sg90'}
  ];
  const root=new THREE.Group(),camera=new THREE.PerspectiveCamera(42,1,.1,100);
  camera.position.set(8,6,10);camera.lookAt(0,1,0);camera.updateMatrixWorld();
  const markers=new Map();for(const slot of slots){const marker=new THREE.Mesh(new THREE.SphereGeometry(.13,12,8));
    marker.position.set(slot.x,slot.y+.23,slot.z);marker.userData.slotId=String(slot.id);
    root.add(marker);markers.set(String(slot.id),marker);}
  root.updateMatrixWorld(true);
  const basePoint=new THREE.Vector3(0,.35,0).project(camera);
  const pointer=new THREE.Vector2(),raycaster=new THREE.Raycaster();
  const context={THREE,Math,root,camera,markers,objects:new Map(),selectedInstance:'22',selectedSlot:null,
    orientation:'Y0',busy:false,exploded:false,options:{matchComponentSlots:true},
    layout:{canEdit:true,instances:[{id:'22',componentId:'sg90'}],
      placements:[{instanceId:'11',slotId:'1'}],spec:{slots}},
    canvas:{getBoundingClientRect:()=>({left:0,top:0,width:512,height:512})},
    pointer,raycaster,previewOrientation(){},highlight(){},message(){},choose(){},selectSlot(){},
    ray(event){pointer.set(event.clientX/256-1,1-event.clientY/256);raycaster.setFromCamera(pointer,camera);}};
  context.selectSlot=slotId=>{context.selectedSlot=String(slotId);};
  vm.createContext(context);
  const downStart=source.indexOf('  function pointerDown('),helperEnd=source.indexOf('  function pointerMove(');
  vm.runInContext(source.slice(downStart,helperEnd),context);
  context.pointerDown({clientX:(basePoint.x+1)*256,clientY:(1-basePoint.y)*256,pointerId:1});
  assert.equal(context.selectedSlot,'2');
});
test('shared design-room placement retains its server-defined marker selection',()=>{
  const slot={id:'1:base',x:0,y:.2,z:0,label:'Vị trí nền'};
  const root=new THREE.Group(),camera=new THREE.PerspectiveCamera(42,1,.1,100);
  camera.position.set(8,6,10);camera.lookAt(0,1,0);camera.updateMatrixWorld();
  const marker=new THREE.Mesh(new THREE.SphereGeometry(.13,12,8));
  marker.position.set(slot.x,slot.y+.23,slot.z);marker.userData.slotId=slot.id;root.add(marker);root.updateMatrixWorld(true);
  const target=new THREE.Vector3(slot.x,slot.y+.23,slot.z).project(camera);
  const pointer=new THREE.Vector2(),raycaster=new THREE.Raycaster();
  const context={THREE,Math,root,camera,markers:new Map([[slot.id,marker]]),objects:new Map(),
    selectedInstance:'22',selectedSlot:null,orientation:'Y0',busy:false,exploded:false,
    options:{matchComponentSlots:false},layout:{canEdit:true,instances:[{id:'22',componentId:'sg90'}],
      placements:[],spec:{slots:[slot]}},canvas:{getBoundingClientRect:()=>({left:0,top:0,width:512,height:512})},
    pointer,raycaster,previewOrientation(){},highlight(){},message(){},choose(){},selectSlot(){},
    ray(event){pointer.set(event.clientX/256-1,1-event.clientY/256);raycaster.setFromCamera(pointer,camera);}};
  context.selectSlot=slotId=>{context.selectedSlot=String(slotId);};
  vm.createContext(context);
  const start=source.indexOf('  function pointerDown('),end=source.indexOf('  function findPreviewSlot(',start);
  vm.runInContext(source.slice(start,end),context);
  context.pointerDown({clientX:(target.x+1)*256,clientY:(1-target.y)*256,pointerId:1});
  assert.equal(context.selectedSlot,'1:base');
});
test('detailed camera framing contains tray and tall mounts at different canvas ratios',()=>{
  const controller=fs.readFileSync(path.resolve(__dirname,'../../assets/js/assembly-3d.js'),'utf8');
  const start=controller.indexOf('  function updateCamera()'),end=controller.indexOf('  function renderPartsPanel()',start);
  for(const aspect of [.7,1,2]){
    const robotGroup=new THREE.Group(),camera=new THREE.PerspectiveCamera(42,aspect,.1,100);
    for(const [x,y,z]of [[-2.4,.9,2.9],[2.1,.9,6.1],[0,3.4,0]]){
      const object=new THREE.Mesh(new THREE.BoxGeometry(.8,.8,.8));object.position.set(x,y,z);robotGroup.add(object);
    }
    const context={THREE,Math,robotGroup,camera,placementView:{},model:{id:'mini-arm'},
      cameraState:{target:new THREE.Vector3(),radius:10,azimuth:.75,polar:1.02}};
    vm.createContext(context);vm.runInContext(controller.slice(start,end),context);context.focusRobot();camera.updateMatrixWorld();
    for(const object of robotGroup.children){const point=object.position.clone().project(camera);
      assert.ok(Math.abs(point.x)<1&&Math.abs(point.y)<1,'object outside canvas ratio '+aspect);}
  }
});
test('selecting the mini-arm Arduino applies its only server-approved orientation',()=>{
  const start=source.indexOf('  function selectedItem('),end=source.indexOf('  function rotate(',start);
  assert.ok(start>=0&&end>start,'placement orientation selection helpers are present');
  const context={Math,allOrientationCodes:['Y0','Y90','Y180','Y270'],options:{matchComponentSlots:true},layout:{canEdit:true,
    instances:[{id:'51',componentId:'sg90'},{id:'52',componentId:'arduino-uno'}],
    placements:[{instanceId:'51',slotId:'servo-4'}],spec:{slots:[
      {id:'servo-4',componentId:'sg90',label:'Servo 4',orientations:['Y0']},
      {id:'arduino',componentId:'arduino-uno',label:'Arduino Uno',orientations:['Y90']}
    ]}},selectedInstance:'51',selectedSlot:'servo-4',selectedObject:null,orientation:'Y0',
    objects:new Map(),directions:{},rotateLeft:{},rotateRight:{},renderedOrientations:[],resetFieldset(){},
    updateRotationControls(){},radio(){},previewOrientation(){},highlight(){},message(){}};
  context.radio=(_fieldset,_name,value)=>context.renderedOrientations.push(value);
  vm.createContext(context);
  vm.runInContext(source.slice(start,end),context);
  context.choose('52');
  assert.equal(context.selectedSlot,null,'the previous servo mount is not reused for the Arduino');
  context.renderedOrientations=[];
  context.selectSlot('arduino');
  assert.equal(context.orientation,'Y90');
  assert.deepEqual(context.renderedOrientations,['Y90']);
});

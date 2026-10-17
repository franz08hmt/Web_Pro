'use strict';
const fs=require('node:fs'),path=require('node:path'),vm=require('node:vm');
const assert=require('node:assert/strict'),test=require('node:test');
const THREE=require('../../assets/vendor/three/three.min.js');
const source=fs.readFileSync(path.resolve(__dirname,'../../assets/js/assembly-3d-placement.js'),'utf8');
function dragAt(slot){
  const slots=[{id:1,x:0,y:.1,z:0,label:'Nền'},{id:2,x:0,y:2.5,z:0,label:'Servo phía trên'}];
  const root=new THREE.Group(),camera=new THREE.PerspectiveCamera(42,1,.1,100);
  camera.position.set(8,6,10);camera.lookAt(0,1,0);camera.updateMatrixWorld();
  const markers=new Map();for(const item of slots){const marker=new THREE.Object3D();marker.position.set(item.x,item.y+.23,item.z);
    root.add(marker);markers.set(String(item.id),marker);}
  root.updateMatrixWorld(true);const target=new THREE.Vector3(slot.x,slot.y+.23,slot.z).project(camera);
  const raycaster=new THREE.Raycaster(),pointer=new THREE.Vector2();
  const context={THREE,Math,root,camera,markers,layout:{spec:{slots}},selectedSlot:null,
    canvas:{getBoundingClientRect:()=>({left:0,top:0,width:512,height:512})},
    drag:{object:new THREE.Object3D(),pointerId:1},plane:new THREE.Plane(new THREE.Vector3(0,1,0),-.9),
    point:new THREE.Vector3(),raycaster,previewOrientation(){},highlight(){},message(){},ray(event){
      pointer.set(event.clientX/256-1,1-event.clientY/256);raycaster.setFromCamera(pointer,camera);
    }};
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

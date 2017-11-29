# play SCENE

This is an example Play application with a SCENE (`0.10.8-rc1`) engine. It has a simple Entity-Sensor model which can be managed through a RESTFul Api.

The engine deploys a single KnowledgeBase with every DRL declared in `conf/scene/`. It goes with a simple `Fever` situation scenario for the sake of demonstration.

### RESTFul API

##### ADD ENTITY
**POST** `/entities`

```json
{ 
  "name": "pereirazc",
  "type": "Person"
} 
```
##### ADD SENSOR FOR AN ENTITY
**POST** `/entities/:entityId`
```json
{ 
  "label": "temperature",
  "default": 36.5
} 
```
##### UPDATE SENSOR VALUE
**POST** `/entities/:entityId/sensors/:sensorId/update`
```json
{ 
  "value": 37.5
} 
```

##### WEBSOCKET SUBSCRIPTION

You can subscribe to be notified of every identified situation (activation/deactivation) by the application

**GET** `/subscribe`
```json
{
  "type" : "activation",
  "timestamp" : 1511981221538,
  "situation" : {
    "rid" : -307100164,
    "type" : "scene.Fever",
    "active" : true,
    "started" : 1511981221538
  },
  "participations" : [ 
      {
        "id" : 1,
        "type" : "Person",
        "as" : "febrile"
      } 
  ]
} 
```


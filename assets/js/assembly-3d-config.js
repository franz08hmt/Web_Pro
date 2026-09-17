"use strict";

window.ASSEMBLY_3D_CONFIG = {

    "line-follower": {

        "chassis-2wd": {
            target: {
                x: 0,
                y: 0.35,
                z: 0
            }
        },

        "arduino-uno": {
            target: {
                x: 0,
                y: 0.65,
                z: 0
            }
        },

        "dc-motor": {
            target: [
                {
                    x: -1.25,
                    y: 0.45,
                    z: 0
                },
                {
                    x: 1.25,
                    y: 0.45,
                    z: 0
                }
            ]
        },

        "wheel": {
            target: [
                {
                    x: -1.65,
                    y: 0.3,
                    z: 0
                },
                {
                    x: 1.65,
                    y: 0.3,
                    z: 0
                }
            ]
        },

        "caster-wheel": {
            target: {
                x: 0,
                y: 0.15,
                z: 0.75
            }
        },

        "line-sensor": {
            target: [
                {
                    x: -0.55,
                    y: 0.15,
                    z: -1.0
                },
                {
                    x: 0.55,
                    y: 0.15,
                    z: -1.0
                }
            ]
        },

        "l298n": {
            target: {
                x: 0,
                y: 0.65,
                z: 0.65
            }
        },

        "battery-holder": {
            target: {
                x: 0,
                y: 0.65,
                z: -0.65
            }
        }
    }
};

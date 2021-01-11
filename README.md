# REACT: Distributed Mobile Microservice Execution Enabled by Efficient Inter-Process Communication 

This repository contains the source code for the above paper.

URL: https://arxiv.org/pdf/2101.00902.pdf

REACT library extends Google Volley library (Android) https://github.com/google/volley, adding IPC mechanisms to allow device local resources and processes/threads to be addressed by HTTP names and transfer data between those components over Android IPC. It also allows dynamic switching between IPC requests and network requests at runtime to enable dynamic offloading of android software components to the network and, switching back to local IPC requests.

When transferring data between app components, REACT overcomes bandwidth limitations of underlying Android IPC and unnecessary copying of data by storing the transferred intra-app data in an application-layer heap automatically at runtime, and transferring only references to those memory blocks (then, automatically retrieving data in corresponding memory blocks in the app-layer heap on delivery at the receiver). The user can also manually store data in the app-layer heap (then communicate the references to other in-app components) and, manually retrieve, amend data and delete memory blocks, emulating operations of OS-level heap implementations.

Proof-of-concepts demonstrated at Mobile World Congress 2018, 2019, 2020 (cancelled due to COVID-19).

## How To
TBA

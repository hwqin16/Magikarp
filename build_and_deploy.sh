#!/bin/bash

docker build -t gcr.io/magikarp-295201/magikarp-server:latest .

docker push gcr.io/magikarp-295201/magikarp-server:latest

kubectl delete -f magikarp-deployment.yaml

kubectl create -f magikarp-deployment.yaml
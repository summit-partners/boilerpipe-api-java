PROJECT=boilerpipe-api
REGISTRY=742882698179.dkr.ecr.us-east-1.amazonaws.com
ENVIRONMENT=dev
REV=$(shell git rev-parse --short HEAD)
TAG=$(REGISTRY)/$(PROJECT):$(REV)
TAG_LATEST=$(REGISTRY)/$(PROJECT):$(ENVIRONMENT)
AWS=aws --region=us-east-1 --profile ecs-deploy

unexport AWS_PROFILE

build:
	docker build -t $(TAG) -t $(TAG_LATEST) .

login:
	$$($(AWS) ecr get-login | cut -d ' ' -f 1-6,9)

push: build login
	docker push $(TAG) || make help
	docker push $(TAG_LATEST) || make help

run:
	docker run -p3000:3000 $(TAG_LATEST)

help:
	@echo 'Run: refresh-aws ecs-deploy if your credentials are out of date.'

deploy: push
	git tag deploy-$$(date +'%Y%m%d-%H%M%S')
	git push --tags

package main

import (
	"log"

	"golang-sidecar/pkg/constant"
	route "golang-sidecar/pkg/route"

	"github.com/gin-gonic/gin"
)

func main() {

	if err := route.SettingRouter(gin.Default()).Run(constant.ServicePort); err != nil {
		log.Fatal("Golang sidecar service start failed.")
	}

	log.Println("Golang sidecar service listener in localhost:8060 ...")

}

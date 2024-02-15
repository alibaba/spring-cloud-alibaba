package router

import (
	"golang-sidecar/pkg/constant"
	"net/http"

	"github.com/gin-gonic/gin"
)

func SettingRouter(r *gin.Engine) *gin.Engine {

	v1 := r.Group("/api/v1")
	{
		// Health check request
		v1.GET("health-check", func(ctx *gin.Context) {
			ctx.JSON(http.StatusOK, constant.Success)
		})
		v1.GET("test", func(ctx *gin.Context) {
			ctx.JSON(http.StatusOK, constant.Test)
		})
	}

	return r
}

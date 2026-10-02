import Vue from "vue";
import Router from "vue-router";

Vue.use(Router);

export default new Router({
  mode: "history",
  routes: [
    { path: "/", redirect: "/dashboard" },
    {
      path: "/dashboard",
      component: () =>
        import(/* webpackChunkName: "dashboard" */ "../views/DashboardView.vue")
    },
    {
      path: "/data-governance",
      component: () =>
        import(
          /* webpackChunkName: "data-governance" */ "../views/DataGovernanceView.vue"
        )
    },
    {
      path: "/alerts",
      component: () =>
        import(/* webpackChunkName: "alerts" */ "../views/AlertCenterView.vue")
    },
    {
      path: "/templates",
      component: () =>
        import(/* webpackChunkName: "templates" */ "../views/TemplateCenterView.vue")
    }
  ]
});

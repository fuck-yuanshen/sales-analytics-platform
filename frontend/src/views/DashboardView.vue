<template>
  <div class="page-card">
    <h2 class="page-title">销售分析看板</h2>

    <el-form :inline="true" :model="filters" class="filters">
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
        />
      </el-form-item>
      <el-form-item label="对比时间">
        <el-date-picker
          v-model="compareRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
        />
      </el-form-item>
      <el-form-item label="统计粒度">
        <el-select v-model="filters.granularity" style="width: 120px">
          <el-option label="日" value="DAY" />
          <el-option label="周" value="WEEK" />
          <el-option label="月" value="MONTH" />
          <el-option label="季度" value="QUARTER" />
          <el-option label="年" value="YEAR" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态">
        <el-select v-model="filters.paymentStatuses" multiple clearable style="width: 160px">
          <el-option label="已支付" value="PAID" />
          <el-option label="已下单" value="PLACED" />
        </el-select>
      </el-form-item>
      <el-form-item label="订单类型">
        <el-select v-model="filters.orderTypes" multiple clearable style="width: 160px">
          <el-option label="普通订单" value="NORMAL" />
          <el-option label="团购订单" value="GROUP" />
        </el-select>
      </el-form-item>
      <el-form-item label="用户标签">
        <el-select v-model="filters.userTags" multiple clearable style="width: 180px">
          <el-option label="新客" value="NEW" />
          <el-option label="复购客" value="RETURNING" />
        </el-select>
      </el-form-item>
      <el-form-item label="消费层级">
        <el-select v-model="filters.spendingTiers" multiple clearable style="width: 180px">
          <el-option label="L1（低）" value="L1" />
          <el-option label="L2（中）" value="L2" />
          <el-option label="L3（高）" value="L3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadAll">刷新数据</el-button>
      </el-form-item>
    </el-form>

    <div class="metrics-grid">
      <div class="metric-card accent-1">
        <span>销售总额</span>
        <strong>{{ formatNumber(summary.salesTotal) }}</strong>
      </div>
      <div class="metric-card accent-2">
        <span>环比变化</span>
        <strong>{{ formatRate(summary.salesMomChangeRate) }}</strong>
      </div>
      <div class="metric-card accent-3">
        <span>同比变化</span>
        <strong>{{ formatRate(summary.salesYoyChangeRate) }}</strong>
      </div>
      <div class="metric-card accent-4">
        <span>销量</span>
        <strong>{{ formatNumber(summary.salesVolume) }}</strong>
      </div>
      <div class="metric-card accent-5">
        <span>客单价</span>
        <strong>{{ formatNumber(summary.averageOrderValue) }}</strong>
      </div>
      <div class="metric-card accent-6">
        <span>复购率 / 转化率</span>
        <strong>{{ formatRate(summary.repurchaseRate) }} / {{ formatRate(summary.orderConversionRate) }}</strong>
      </div>
    </div>

    <div class="chart-grid">
      <div class="chart-box">
        <div class="chart-header">
          <h3>销售趋势</h3>
          <div>
            <el-button size="mini" @click="exportChart('trend', 'png')">PNG</el-button>
            <el-button size="mini" @click="exportChart('trend', 'jpg')">JPG</el-button>
          </div>
        </div>
        <div ref="trendChart" class="chart" />
      </div>
      <div class="chart-box">
        <div class="chart-header">
          <h3>商品销量 Top10</h3>
          <div>
            <el-button size="mini" @click="exportChart('top', 'png')">PNG</el-button>
            <el-button size="mini" @click="exportChart('top', 'jpg')">JPG</el-button>
          </div>
        </div>
        <div ref="topChart" class="chart" />
      </div>
      <div class="chart-box">
        <div class="chart-header">
          <h3>用户标签分布</h3>
          <div>
            <el-button size="mini" @click="exportChart('tag', 'png')">PNG</el-button>
            <el-button size="mini" @click="exportChart('tag', 'jpg')">JPG</el-button>
          </div>
        </div>
        <div ref="tagChart" class="chart" />
      </div>
      <div class="chart-box">
        <div class="chart-header">
          <h3>区域热力排行（支持下钻）</h3>
          <div>
            <el-button size="mini" @click="exportChart('geo', 'png')">PNG</el-button>
            <el-button size="mini" @click="exportChart('geo', 'jpg')">JPG</el-button>
          </div>
        </div>
        <div ref="geoChart" class="chart" />
      </div>
    </div>

    <div class="drill-panel">
      <el-tag type="success" size="small">下钻层级：{{ drillLevelLabel(drillLevel) }}</el-tag>
      <el-button size="mini" @click="resetDrill">重置下钻</el-button>
      <el-table :data="drillData" size="small" style="margin-top: 10px">
        <el-table-column prop="name" label="地区" />
        <el-table-column prop="value" label="销售额" />
      </el-table>
    </div>
  </div>
</template>

<script>
import * as echarts from "echarts";
import {
  fetchSummary,
  fetchTrend,
  fetchTopProducts,
  fetchTagDistribution,
  fetchGeoDistribution,
  fetchDrillDown
} from "../api";

export default {
  name: "DashboardView",
  data() {
    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - 30);
    return {
      filters: {
        granularity: "DAY",
        paymentStatuses: ["PAID"],
        orderTypes: [],
        userTags: [],
        spendingTiers: []
      },
      dateRange: [this.formatDate(start), this.formatDate(end)],
      compareRange: [],
      summary: {},
      trend: [],
      topProducts: [],
      tags: [],
      geo: [],
      drillData: [],
      drillLevel: "PROVINCE",
      parentRegion: "",
      chartInstances: {}
    };
  },
  mounted() {
    this.loadAll();
    window.addEventListener("resize", this.resizeCharts);
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.resizeCharts);
    this.disposeCharts();
  },
  methods: {
    formatDate(date) {
      const yyyy = date.getFullYear();
      const mm = String(date.getMonth() + 1).padStart(2, "0");
      const dd = String(date.getDate()).padStart(2, "0");
      return `${yyyy}-${mm}-${dd}`;
    },
    formatNumber(value) {
      return Number(value || 0).toLocaleString();
    },
    formatRate(value) {
      return `${Number(value || 0).toFixed(2)}%`;
    },
    drillLevelLabel(level) {
      if (level === "PROVINCE") return "省";
      if (level === "CITY") return "市";
      if (level === "DISTRICT") return "区/县";
      return level;
    },
    buildPayload() {
      const payload = {
        startDate: this.dateRange[0],
        endDate: this.dateRange[1],
        granularity: this.filters.granularity,
        paymentStatuses: this.filters.paymentStatuses,
        orderTypes: this.filters.orderTypes,
        userTags: this.filters.userTags,
        spendingTiers: this.filters.spendingTiers
      };
      if (this.compareRange && this.compareRange.length === 2) {
        payload.compareStartDate = this.compareRange[0];
        payload.compareEndDate = this.compareRange[1];
      }
      return payload;
    },
    async loadAll() {
      try {
        const payload = this.buildPayload();
        const [summaryRes, trendRes, topRes, tagRes, geoRes] = await Promise.all([
          fetchSummary(payload),
          fetchTrend(payload),
          fetchTopProducts(payload),
          fetchTagDistribution(payload),
          fetchGeoDistribution("PROVINCE", payload)
        ]);

        this.summary = summaryRes.data;
        this.trend = trendRes.data;
        this.topProducts = topRes.data;
        this.tags = tagRes.data;
        this.geo = geoRes.data;
        await this.loadDrill();

        this.$nextTick(() => {
          this.disposeCharts();
          this.renderTrend();
          this.renderTop();
          this.renderTag();
          this.renderGeo();
        });
      } catch (error) {
        this.$message.error(error.message || "加载分析数据失败");
      }
    },
    async loadDrill() {
      const payload = {
        level: this.drillLevel,
        parentRegion: this.parentRegion,
        filter: this.buildPayload()
      };
      const res = await fetchDrillDown(payload);
      this.drillData = res.data;
    },
    renderTrend() {
      const chart = echarts.init(this.$refs.trendChart);
      chart.setOption({
        tooltip: { trigger: "axis" },
        legend: { data: ["销售额"] },
        dataZoom: [{ type: "inside" }, { type: "slider" }],
        xAxis: { type: "category", data: this.trend.map((i) => i.period) },
        yAxis: { type: "value" },
        series: [
          {
            name: "销售额",
            type: "line",
            smooth: true,
            areaStyle: {},
            data: this.trend.map((i) => Number(i.salesAmount || 0))
          }
        ]
      });
      this.chartInstances.trend = chart;
    },
    renderTop() {
      const chart = echarts.init(this.$refs.topChart);
      chart.setOption({
        tooltip: { trigger: "axis" },
        xAxis: { type: "value" },
        yAxis: { type: "category", data: this.topProducts.map((i) => i.productName) },
        series: [
          {
            name: "销量",
            type: "bar",
            data: this.topProducts.map((i) => Number(i.quantity || 0)),
            itemStyle: { color: "#2a9d8f" }
          }
        ]
      });
      this.chartInstances.top = chart;
    },
    renderTag() {
      const chart = echarts.init(this.$refs.tagChart);
      chart.setOption({
        tooltip: { trigger: "item" },
        legend: { bottom: 0 },
        series: [
          {
            name: "标签",
            type: "pie",
            radius: ["40%", "70%"],
            data: this.tags.map((i) => ({ name: i.label, value: i.value }))
          }
        ]
      });
      this.chartInstances.tag = chart;
    },
    renderGeo() {
      const chart = echarts.init(this.$refs.geoChart);
      chart.setOption({
        tooltip: { trigger: "axis" },
        visualMap: {
          orient: "horizontal",
          left: "center",
          min: 0,
          max: Math.max(...this.geo.map((i) => Number(i.salesAmount || 0)), 1),
          inRange: {
            color: ["#fef3e2", "#f39c5b", "#d95f02"]
          }
        },
        xAxis: { type: "category", data: this.geo.map((i) => i.regionName) },
        yAxis: { type: "value" },
        series: [
          {
            name: "销售额",
            type: "bar",
            data: this.geo.map((i) => Number(i.salesAmount || 0)),
            itemStyle: { borderRadius: [6, 6, 0, 0] }
          }
        ]
      });
      chart.on("click", async (params) => {
        if (this.drillLevel === "PROVINCE") {
          this.drillLevel = "CITY";
          this.parentRegion = params.name;
        } else if (this.drillLevel === "CITY") {
          this.drillLevel = "DISTRICT";
          this.parentRegion = params.name;
        }
        await this.loadDrill();
      });
      this.chartInstances.geo = chart;
    },
    resetDrill() {
      this.drillLevel = "PROVINCE";
      this.parentRegion = "";
      this.loadDrill();
    },
    exportChart(chartKey, fileType) {
      const chart = this.chartInstances[chartKey];
      if (!chart) {
        this.$message.warning("图表尚未准备完成");
        return;
      }
      const mime = fileType === "jpg" ? "jpeg" : "png";
      const dataUrl = chart.getDataURL({
        pixelRatio: 2,
        backgroundColor: "#fff",
        type: mime
      });
      const link = document.createElement("a");
      link.href = dataUrl;
      link.download = `${chartKey}-图表.${fileType}`;
      link.click();
    },
    resizeCharts() {
      Object.values(this.chartInstances).forEach((chart) => chart.resize());
    },
    disposeCharts() {
      Object.values(this.chartInstances).forEach((chart) => chart.dispose());
      this.chartInstances = {};
    }
  }
};
</script>

<style scoped>
.filters {
  margin-bottom: 16px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.metric-card {
  border-radius: 10px;
  padding: 12px;
  color: #fff;
}

.metric-card span {
  display: block;
  opacity: 0.9;
  font-size: 13px;
}

.metric-card strong {
  display: block;
  margin-top: 6px;
  font-size: 22px;
}

.accent-1 { background: linear-gradient(135deg, #355c7d, #6c5b7b); }
.accent-2 { background: linear-gradient(135deg, #2a9d8f, #55c4a4); }
.accent-3 { background: linear-gradient(135deg, #e76f51, #f4a261); }
.accent-4 { background: linear-gradient(135deg, #264653, #2a9d8f); }
.accent-5 { background: linear-gradient(135deg, #7b2cbf, #c77dff); }
.accent-6 { background: linear-gradient(135deg, #4361ee, #4cc9f0); }

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(300px, 1fr));
  gap: 12px;
}

.chart-box {
  border: 1px solid #e6ebf2;
  border-radius: 12px;
  padding: 10px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chart-box h3 {
  margin: 0;
  font-size: 15px;
}

.chart {
  height: 280px;
}

.drill-panel {
  margin-top: 14px;
  border: 1px solid #e6ebf2;
  border-radius: 12px;
  padding: 10px;
}

@media (max-width: 960px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(140px, 1fr));
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>


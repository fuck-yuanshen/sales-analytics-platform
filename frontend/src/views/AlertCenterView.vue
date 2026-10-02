<template>
  <div class="page-card">
    <h2 class="page-title">预警中心</h2>

    <el-row :gutter="14">
      <el-col :span="12">
        <el-card shadow="never">
          <div class="row-head">
            <h3>预警规则</h3>
            <div class="action-buttons">
              <el-button
                type="danger"
                size="mini"
                :disabled="selectedRuleIds.length === 0"
                @click="removeSelected"
              >
                删除规则
              </el-button>
              <el-button type="primary" size="mini" @click="showDialog(null)">新建规则</el-button>
            </div>
          </div>
          <el-table
            ref="rulesTable"
            :data="rules"
            size="small"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="ruleName" label="规则名称" min-width="140" />
            <el-table-column prop="metricCode" label="指标" min-width="140" />
            <el-table-column prop="comparator" label="比较符" width="100" />
            <el-table-column prop="threshold" label="阈值" width="100" />
            <el-table-column prop="enabled" label="启用" width="80">
              <template slot-scope="scope">
                <el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? "是" : "否" }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160">
              <template slot-scope="scope">
                <el-button type="text" @click="showDialog(scope.row)">编辑</el-button>
                <el-button type="text" @click="remove(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never">
          <div class="row-head">
            <h3>预警事件</h3>
            <el-button type="warning" size="mini" @click="evaluate">立即评估</el-button>
          </div>
          <el-table :data="events" size="small">
            <el-table-column prop="metricCode" label="指标" min-width="130" />
            <el-table-column prop="metricValue" label="指标值" width="90" />
            <el-table-column prop="severity" label="严重级别" width="90" />
            <el-table-column prop="periodLabel" label="统计周期" width="110" />
            <el-table-column prop="message" label="预警信息" min-width="170" show-overflow-tooltip />
            <el-table-column prop="triggeredAt" label="触发时间" min-width="150" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog :title="editingId ? '编辑规则' : '新建规则'" :visible.sync="dialogVisible" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="规则名称"><el-input v-model="form.ruleName" /></el-form-item>
        <el-form-item label="指标">
          <el-select v-model="form.metricCode">
            <el-option label="销售额环比变化" value="SALES_MOM_CHANGE" />
            <el-option label="销售总额" value="SALES_TOTAL" />
            <el-option label="复购率" value="REPURCHASE_RATE" />
            <el-option label="订单转化率" value="ORDER_CONVERSION_RATE" />
            <el-option label="客单价" value="AVERAGE_ORDER_VALUE" />
          </el-select>
        </el-form-item>
        <el-form-item label="比较符">
          <el-select v-model="form.comparator">
            <el-option label=">" value="GREATER_THAN" />
            <el-option label=">=" value="GREATER_EQUAL" />
            <el-option label="<" value="LESS_THAN" />
            <el-option label="<=" value="LESS_EQUAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值"><el-input-number v-model="form.threshold" :precision="2" /></el-form-item>
        <el-form-item label="是否启用"><el-switch v-model="form.enabled" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  listAlertRules,
  createAlertRule,
  updateAlertRule,
  deleteAlertRule,
  listAlertEvents,
  evaluateAlerts
} from "../api";

export default {
  name: "AlertCenterView",
  data() {
    return {
      rules: [],
      events: [],
      selectedRuleIds: [],
      dialogVisible: false,
      editingId: null,
      form: {
        ruleName: "",
        metricCode: "SALES_MOM_CHANGE",
        comparator: "LESS_THAN",
        threshold: -20,
        enabled: true,
        description: ""
      }
    };
  },
  mounted() {
    this.load();
  },
  methods: {
    async load() {
      const [rulesRes, eventsRes] = await Promise.all([listAlertRules(), listAlertEvents()]);
      this.rules = rulesRes.data;
      this.events = eventsRes.data;
      this.selectedRuleIds = [];
      if (this.$refs.rulesTable) {
        this.$refs.rulesTable.clearSelection();
      }
    },
    handleSelectionChange(rows) {
      this.selectedRuleIds = rows.map((row) => row.id);
    },
    showDialog(row) {
      this.dialogVisible = true;
      if (!row) {
        this.editingId = null;
        this.form = {
          ruleName: "",
          metricCode: "SALES_MOM_CHANGE",
          comparator: "LESS_THAN",
          threshold: -20,
          enabled: true,
          description: ""
        };
        return;
      }
      this.editingId = row.id;
      this.form = {
        ruleName: row.ruleName,
        metricCode: row.metricCode,
        comparator: row.comparator,
        threshold: Number(row.threshold),
        enabled: !!row.enabled,
        description: row.description
      };
    },
    async submit() {
      if (this.editingId) {
        await updateAlertRule(this.editingId, this.form);
      } else {
        await createAlertRule(this.form);
      }
      this.dialogVisible = false;
      this.$message.success("规则保存成功");
      this.load();
    },
    async remove(id) {
      await deleteAlertRule(id);
      this.$message.success("规则删除成功");
      this.load();
    },
    async removeSelected() {
      if (this.selectedRuleIds.length === 0) {
        this.$message.warning("请先选择要删除的规则");
        return;
      }
      try {
        await this.$confirm(`确认删除已选中的 ${this.selectedRuleIds.length} 条规则吗？`, "提示", {
          type: "warning",
          confirmButtonText: "确认",
          cancelButtonText: "取消"
        });
      } catch (error) {
        return;
      }

      const results = await Promise.allSettled(this.selectedRuleIds.map((id) => deleteAlertRule(id)));
      const successCount = results.filter((item) => item.status === "fulfilled").length;
      const failedCount = results.length - successCount;
      if (failedCount === 0) {
        this.$message.success(`已删除 ${successCount} 条规则`);
      } else {
        this.$message.warning(`已删除 ${successCount} 条，失败 ${failedCount} 条`);
      }
      this.load();
    },
    async evaluate() {
      const res = await evaluateAlerts();
      this.$message.success(`评估完成，触发 ${res.data} 条预警事件`);
      this.load();
    }
  }
};
</script>

<style scoped>
.row-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

h3 {
  margin: 0;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>


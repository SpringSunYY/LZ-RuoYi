<template>
  <el-card>
    <el-tabs :tab-position="tabPosition" type="border-card" style="margin-bottom: 30px;min-height: 600px">
      <el-tab-pane
        v-for="(column,index) in columns"
        :key="column.columnId"
        :name="column.columnName"
        v-if="!column.pk"
      >
        <span slot="label"><svg-icon v-if="column.required" icon-class="star" style="color: red"/> {{
            column.columnName
          }}</span>
        <el-form :ref="'form' + index" :model="tableColumnValues[index]" label-width="80px" size="mini"
                 style="width: 500px; margin: 0 auto;"
        >
          <el-form-item label="字段名称">
            <span>{{ tableColumnValues[index].columnName }}</span>
          </el-form-item>
          <el-form-item label="字段描述">
            <span>{{ tableColumnValues[index].columnComment }}</span>
          </el-form-item>
          <el-form-item label="物理类型">
            <span> {{ tableColumnValues[index].columnType }}</span>
          </el-form-item>
          <el-form-item label="Java类型">
            <span>{{ tableColumnValues[index].javaType }}</span>
          </el-form-item>
          <el-form-item label="Java属性">
            <span>{{ tableColumnValues[index].javaField }}</span>
          </el-form-item>
          <el-form-item label="是否唯一">
            <el-radio-group v-model="tableColumnValues[index].isSole">
              <el-radio :label="0">否</el-radio>
              <el-radio :label="1">是</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="column.htmlType==='textarea'"
                        label="生成数值"
                        :rules="[
                      { required: column.required, message: `${column.columnComment} 不能为空`, trigger: 'blur' }
                    ]"
          >
            <el-input v-model="tableColumnValues[index].value" type="textarea" placeholder="请输入生成数值"/>
          </el-form-item>
          <el-form-item v-if="column.htmlType==='datetime'"
                        label="生成数值"
                        :rules="[
                      { required: column.required, message: `${column.columnComment} 不能为空`, trigger: 'blur' }
                    ]"
          >
            <el-date-picker clearable
                            v-model="tableColumnValues[index].value"
                            type="date"
                            value-format="yyyy-MM-dd"
                            placeholder="请选择日期"
            >
            </el-date-picker>
          </el-form-item>
          <el-form-item v-if="column.htmlType==='imageUpload'"
                        label="生成数值"
                        :rules="[
                      { required: column.required, message: `${column.columnComment} 不能为空`, trigger: 'blur' }
                    ]"
          >
            <image-upload v-model="tableColumnValues[index].value"/>
          </el-form-item>
          <el-form-item v-if="column.htmlType==='fileUpload'"
                        label="生成数值"
                        :rules="[
                      { required: column.required, message: `${column.columnComment} 不能为空`, trigger: 'blur' }
                    ]"
          >
            <file-upload v-model="tableColumnValues[index].value"/>
          </el-form-item>
          <el-form-item v-if="column.htmlType==='editor'"
                        label="生成数值"
                        :rules="[
                      { required: column.required, message: `${column.columnComment} 不能为空`, trigger: 'blur' }
                    ]"
          >
            <editor v-model="tableColumnValues[index].value"/>
          </el-form-item>
          <el-form-item
            label="生成数值"
            :rules="[
                      { required: column.required, message: `${column.columnComment} 不能为空`, trigger: 'blur' }
                    ]"
          >
            <el-input v-model="tableColumnValues[index].value" placeholder="请输入生成数值"/>
          </el-form-item>
        </el-form>
        {{ tableColumnValues[index] }}
      </el-tab-pane>
    </el-tabs>
    <el-form label-width="1000px" :inline="true" class="demo-form-inline">
      <el-form-item label="生成总数">
        <el-input-number :min="1" v-model="genInfo.genNumbers" placeholder="请输入生成总数"></el-input-number>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm()">提交</el-button>
        <el-button @click="close()">返回</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
import { getGenTable, genValue } from '@/api/tool/gen'
import basicInfoForm from './basicInfoForm'
import genInfoForm from './genInfoForm'

export default {
  name: 'GenAddValue',
  components: {
    basicInfoForm,
    genInfoForm
  },
  data() {
    return {
      //生成信息
      genInfo: {
        tableName: {},
        genNumbers: 1,
        tableColumnValues: []
      },
      tabPosition: 'top',
      //生成字段数据
      tableColumnValues: [],
      // 表列信息
      columns: []
    }
  },
  created() {
    const tableId = this.$route.params && this.$route.params.tableId
    if (tableId) {
      // 获取表详细信息
      getGenTable(tableId).then(res => {
        this.columns = res.data.rows
        // 初始化 tableColumnValues，使其长度与 columns 一致
        this.tableColumnValues = this.columns.map((item) => ({
          columnName: item.columnName,
          columnComment: item.columnComment,
          columnType: item.columnType,
          pk: item.pk,
          javaType: item.javaType,
          javaField: item.javaField,
          required: item.required,
          isSole: 0
        }))
        this.genInfo.tableName = res.data.info.tableName
      })
    }
  },
  methods: {
    /** 提交按钮 */
    submitForm() {
      //校验是否有必须需要值但是为空的
      for (const index in this.tableColumnValues) {
        const value = this.tableColumnValues[index]?.value // 获取对应的值
        const column = this.tableColumnValues[index] // 获取对应的列信息
        if (column.required && (!value || value.trim() === '') && !column.pk) {
          // 如果 required 为 true 且值不存在或为空
          this.$message.error(`${column.columnComment}不能为空`)
          return // 直接返回
        }
      }
      this.genInfo.tableColumnValues = this.tableColumnValues
      genValue(this.genInfo).then(res => {
        this.$message.success('成功')
      })
    },
    /** 关闭按钮 */
    close() {
      const obj = { path: '/tool/gen', query: { t: Date.now(), pageNum: this.$route.query.pageNum } }
      this.$tab.closeOpenPage(obj)
    }
  }
}
</script>

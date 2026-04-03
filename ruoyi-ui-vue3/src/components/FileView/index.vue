<template>
  <div v-if="fileUrl">
    <el-tooltip placement="top" effect="light">
      <template #content>
        <div v-for="(file, index) in fileUrl.split(',')"
             :key="index"
             style="text-align: left; padding: 5px;">
          <el-link
              :download="getFileName(file)"
              :href="getFilePath(file)"
              :underline="'never'"
              target="_blank"
              style="font-size: 14px"
          >
            <span style="cursor: pointer;">{{ getFileName(file) }}</span>
          </el-link>
        </div>
      </template>
      <span style="cursor: pointer; color: #409EFF;">{{ text }}</span>
    </el-tooltip>
  </div>
  <div v-else>-</div>
</template>

<script setup name="FileView">
import {getFileName, getFilePath} from "@/utils/ruoyi";

defineProps({
  fileUrl: {
    type: String,
    default: null
  },
  text: {
    type: String,
    default: "查看文件"
  }
});
</script>

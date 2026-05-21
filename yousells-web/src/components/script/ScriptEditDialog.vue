<script setup lang="ts">
import { ref, reactive, watch } from "vue";
import { ElMessage } from "element-plus";
import type { ScriptItem, ScriptCreateRequest, ScriptUpdateRequest } from "@/types/script";
import type { ScriptCategory } from "@/types/script";
import { createScript, updateScript } from "@/api/script";

const props = defineProps<{
  visible: boolean;
  script: ScriptItem | null;
  categories: ScriptCategory[];
}>();

const emit = defineEmits<{
  close: [];
  saved: [];
}>();

const isEdit = ref(false);
const submitting = ref(false);
const formRef = ref();

const form = reactive<ScriptCreateRequest>({
  categoryId: 0,
  title: "",
  content: "",
  applicableScene: null,
  status: "启用"
});

const rules = {
  categoryId: [{ required: true, message: "请选择分类", trigger: "change" }],
  title: [{ required: true, message: "请填写话术标题", trigger: "blur" }],
  content: [{ required: true, message: "请填写话术内容", trigger: "blur" }]
};

watch(
  () => [props.visible, props.script] as const,
  ([v, s]) => {
    if (v) {
      if (s) {
        isEdit.value = true;
        form.categoryId = s.categoryId;
        form.title = s.title;
        form.content = s.content;
        form.applicableScene = s.applicableScene;
        form.status = s.status;
      } else {
        isEdit.value = false;
        form.categoryId = props.categories[0]?.id ?? 0;
        form.title = "";
        form.content = "";
        form.applicableScene = null;
        form.status = "启用";
      }
    }
  }
);

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  submitting.value = true;
  try {
    if (isEdit.value && props.script) {
      await updateScript(props.script.id, form as ScriptUpdateRequest);
      ElMessage.success("话术已更新");
    } else {
      await createScript(form as ScriptCreateRequest);
      ElMessage.success("话术已创建");
    }
    emit("saved");
    emit("close");
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : "操作失败");
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑话术' : '新增话术'"
    width="560px"
    :close-on-click-modal="false"
    @update:model-value="emit('close')"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="90px"
      label-position="top"
    >
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" style="width: 100%">
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.categoryName"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="话术标题" />
      </el-form-item>

      <el-form-item label="适用场景">
        <el-input v-model="form.applicableScene" placeholder="什么场景下使用此话术" />
      </el-form-item>

      <el-form-item label="内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="6"
          placeholder="话术详细内容"
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="form.status" style="width: 100%">
          <el-option label="启用" value="启用" />
          <el-option label="禁用" value="禁用" />
          <el-option label="草稿" value="草稿" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('close')" :disabled="submitting">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">
        {{ isEdit ? "保存修改" : "创建话术" }}
      </el-button>
    </template>
  </el-dialog>
</template>

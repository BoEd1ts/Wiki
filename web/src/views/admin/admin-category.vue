<template>
  <a-layout>
    <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '80px'  }"
    >
      <p>
        <a-form layout="inline" :model="param">
          <a-form-item>
            <a-input v-model:value="param.name" placeholder="名称">
            </a-input>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleQuery({page: 1, size: pagination.pageSize})">
              查询
            </a-button>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="add()">
              新增
            </a-button>
          </a-form-item>
        </a-form>

      </p>
      <!--列,key id,数据category,分页,等待框,分页执行方法-->
      <a-table
          :columns="columns"
          :row-key="record=>record.id"
          :data-source="categorys"
          :pagination="pagination"
          :loading="loading"
          @change="handleTableChange"
      >
        <template #cover="{text:cover}">
          <img class="img-wh" v-if="cover" :src="cover" alt="avatar"/> <!--渲染图片-->
        </template>
        <template v-slot:action="{ text, record }">
          <a-space size="small">
            <a-button type="primary" @click="edit(record)">
              编辑
            </a-button>
            <a-popconfirm
                title="删除后不可恢复！确定？"
                ok-text="是"
                cancel-text="否"
                @confirm="handleDelete(record.id)"
                @cancel="cancel"
            >
              <a-button type="primary" >
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-layout-content>

  </a-layout>
  <a-modal
      title="分类管理"
      v-model:visible="modalVisible"
      :confirm-loading="modalLoading"
      ok-text="ojbk"
      @ok="handleModalOk"
  >
    <!--弹出表单-->
    <a-form :model="category" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="名称">
        <a-input v-model:value="category.name"/>
      </a-form-item>
      <!--下拉菜单-->
      <a-form-item label="父分类">
        <a-input v-model:value="category.parent"/>
      </a-form-item>
      <a-form-item label="顺序">
        <a-input v-model:value="category.sort"/>
      </a-form-item>
    </a-form>

  </a-modal>

</template>

<script lang="ts">
import {defineComponent, onMounted, ref} from 'vue';//写上onMounted VUE3.0 setup集成了 导入ref 做响应式数据
import axios from 'axios';
import {message} from "ant-design-vue";
import {Tool} from "@/util/tool";

export default defineComponent({
  name: 'AdminCategory',
  setup() {
    const param = ref();
    param.value = {};
    const categorys = ref();//响应式数据 获取的书籍实时反馈到页面上
    const pagination = ref({
      current: 1,//当前页
      pageSize: 10,//分页条数
      total: 0
    });

    const loading = ref(false);


    const columns = [
      {
        title: '名称',
        dataIndex: 'name',
      },
      {
        title: '父分类',
        key: 'parent',
        dataIndex: 'parent',
      },
      {
        title: '顺序',
        dataIndex: 'sort',
      },
      {
        title: 'Action',
        key: 'action',
        slots: {customRender: 'action'}
      }
    ];

    /**
     * 数据查询
     **/
    const handleQuery = (params: any) => {
      loading.value = true;
      axios.get("/category/list", {
        params:{
          page:params.page,
          size:params.size,
          name:param.value.name//增加按名字查询
        }
      }).then((response) => {
        loading.value = false;
        const data = response.data;
        if(data.success){

        console.log(data);
        categorys.value = data.content.list;

        //重置分页按钮
        pagination.value.current = params.page;//点第二页的按钮的时候前端 不会刷新 还是第一页的地方 实际我们以及到第二页了
        pagination.value.total=data.content.total;
        }else {
          message.error(data.success);
        }
      });
    };
    /**
     * 表格点击页码时触发
     */
    const handleTableChange = (pagination: any) => {
      console.log("看看自带的分页参数都有啥：" + pagination);
      handleQuery({
        page: pagination.current,
        size: pagination.pageSize
      });
    };

    /**表单*/
    const category=ref({});
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const handleModalOk = () => {
      modalLoading.value = true;
      axios.post("/category/save",category.value).then((response) => {
        modalLoading.value=false;
        const data = response.data;  //commonResp
        if(data.success){
          modalVisible.value = false;

          //重新加载列表
          handleQuery({
            page:pagination.value.current,  //查询当前所在的页
            size:pagination.value.pageSize
          });
        }else {
          message.error(data.message);
        }
      });
    };
    /**
     * 编辑
     */
    const edit = ( record:any ) =>{
      modalVisible .value = true;
      category.value = Tool.copy(record);
    };
    /**
     * 添加
     */
    const add = () =>{
      modalVisible .value = true;
      category.value={};
    };


    /**
     * 删除
     */
    const handleDelete = ( id:number ) =>{

      axios.delete("/category/delete/"+id).then((response)=>{
        const data = response.data;  //commonResp
        if(data.success){
          //重新加载列表
          handleQuery({
            page:pagination.value.current,  //查询当前所在的页
            size:pagination.value.pageSize
          });
        }
      })
    };


    onMounted(() => {
      handleQuery({
        page:1,
        size:pagination.value.pageSize
      });

    });

    return {
      param,//增加按名字查询
      categorys,//表格
      pagination,
      columns,
      loading,
      handleTableChange,
      handleQuery,//增加按名字查询


      edit,
      add,
      handleDelete,

      category,
      modalVisible,
      modalLoading,
      handleModalOk,
    }
  }
});
</script>

<!-- #scoped表示当前组件才有用 -->
<style scoped>
.img-wh {
  width: 50px;
  height: 50px;
  line-height: 50px;
  border-radius: 8%;
  margin: 5px 0;
}

</style>
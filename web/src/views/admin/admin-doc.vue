<template>
  <a-layout>
    <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '80px'  }"
    >
      <a-row :gutter="24">
        <a-col :span="8">
          <p>
            <a-form layout="inline" :model="param">

              <a-form-item>
                <a-button type="primary" @click="handleQuery()">
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
          <!--列,key id,数据doc,分页,等待框,分页执行方法-->
          <a-table
              v-if="level1.length >0"
              :columns="columns"
              :row-key="record=>record.id"
              :data-source="level1"
              :loading="loading"
              :pagination="false"
              size="small"
              :defaultExpandAllRows="true"
          >
            <template #name="{text,record}">
              {{record.sort}} {{text}}
            </template>
            <template v-slot:action="{ text, record }">
              <a-space size="small">
                <a-button type="primary" @click="edit(record)" size="small">
                  编辑
                </a-button>
                <a-popconfirm
                    title="删除后不可恢复！确定？"
                    ok-text="是"
                    cancel-text="否"
                    @confirm="handleDelete(record.id)"
                    @cancel="cancel"
                >
                  <a-button type="primary" size="small">
                    删除
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table>
        </a-col>
        <a-col :span="16">
          <p>
            <a-form layout="inline" :model="param">
              <a-form-item>
                <a-button type="primary" @click="handleSave">
                  保存
                </a-button>
              </a-form-item>

            </a-form>
          </p>
          <!--弹出电子书-->
          <a-form :model="doc" layout="vertical">
            <a-form-item>
              <a-input v-model:value="doc.name" placeholder="名称"/>
            </a-form-item>
            <a-form-item>
              <a-tree-select
                  v-model:value="doc.parent"
                  style="width: 100%"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  :tree-data="treeSelectData"
                  placeholder="请选择父文档"
                  tree-default-expand-all
                  :replaceFields="{title:'name',key:'id',value:'id'}"
              >
              </a-tree-select>
            </a-form-item>
            <a-form-item>
              <a-input v-model:value="doc.sort" placeholder="顺序"/>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handlePreviewContent()">
                <EyeOutlined /> 内容预览
              </a-button>
            </a-form-item>
            <a-form-item>
              <div id="content"></div>
            </a-form-item>
          </a-form>

        </a-col>
      </a-row>

      <a-drawer width="900" placement="right" :closable="false" :visible="drawerVisible" @close="onDrawerClose">
        <div class="wangeditor" :innerHTML="previewHtml"></div>
      </a-drawer>

    </a-layout-content>
  </a-layout>
<!--  <a-modal-->
<!--      title="分类管理"-->
<!--      v-model:visible="modalVisible"-->
<!--      :confirm-loading="modalLoading"-->
<!--      ok-text="ojbk"-->
<!--      @ok="handleModalOk"-->
<!--  >-->

<!--  </a-modal>-->

</template>

<script lang="ts">
import {defineComponent, onMounted, ref} from 'vue';//写上onMounted VUE3.0 setup集成了 导入ref 做响应式数据
import axios from 'axios';
import {message} from "ant-design-vue";
import {Tool} from "@/util/tool";
import {useRoute} from "vue-router";
import E from 'wangeditor';

export default defineComponent({
  name: 'AdminDoc',
  setup() {
    const  route = useRoute()
    console.log("路由:", route);
    console.log( "route.path: ", route.path);
    console.log("route.query: ", route.query);
    console.log( "route.param: ", route.params);
    console.log( "route.fu11Path: ", route.fullPath);
    console.log( "route.name: ", route.name );
    console.log( "route.meta: ", route.meta);
    const param = ref();
    param.value = {};
    const docs = ref();//响应式数据 获取的书籍实时反馈到页面上

    const loading = ref(false);
    //因为树选择组件的属性状态，会随着当前节点编辑的节点而变化  所以单独声明
    const treeSelectData  =ref();
    treeSelectData.value=[];


    const columns = [
      {
        title: '名称',
        dataIndex: 'name',
        slots: {customRender: 'name'}
      },
      {
        title: 'Action',
        key: 'action',
        slots: {customRender: 'action'}
      }
    ];

    const level1 = ref(); //一级分类树，children就是二级分类
    level1.value=[];

    /**
     * 数据查询
     **/
    const handleQuery = () => {
      loading.value = true;
      //如果不清空现有数据，则编辑保存重新加载数据后，再点编辑会显示原来数据
      level1.value=[];//清空现有数据
      axios.get("/doc/all/"+ route.query.ebookId).then((response) => {
        loading.value = false;
        const data = response.data;
        if(data.success){

        console.log(data);
        docs.value = data.content;
        console.log("原始数组：",docs.value);
        level1.value=[];
        level1.value=Tool.array2Tree(docs.value,0);
          console.log("树形结构：",level1);
        //父文档下拉初始化，相当于点击新增
          treeSelectData.value=Tool.copy(level1.value) || [];
        //为选择树添加一个无
          treeSelectData.value.unshift({id:0,name:'无'});
        }else {
          message.error(data.success);
        }
      });
    };


    //--------------表单----------------------
    const doc=ref();
    doc.value={ebookId:route.query.ebookId}; //初始打开文档管理页面，直接新增文档时，会报错:电子书不能;为空
    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const editor = new E('#content');
    editor.config.zIndex=0;


    const handleSave = () => {
      modalLoading.value = true;
      doc.value.content = editor.txt.html()
      axios.post("/doc/save",doc.value).then((response) => {
        modalLoading.value=false;
        const data = response.data;  //commonResp
        if(data.success){
          // modalVisible.value = false;
          message.success("保存成功")

          //重新加载列表
          handleQuery();
        }else {
          message.error(data.message);
        }
      });
    };
    /**
     * 将某节点及其子孙节点全部置为dosable
     */
    const setDisable=(treeSelectData:any,id:any)=>{
      //consoLe.Log(treeselectData, id );
      // 逦历数组，即逦历某一层节点
      for (let i =0;i < treeSelectData.length;i++){
        const node = treeSelectData[i];
        if (node.id === id){
          // 如果当前节点就是目标节点
          console.log("disable",node);
          // 将目标节点设置为disabled
          node.disabled = true;

          //遍历所有子节点，将所有子节点全部加上disabled
          const children = node.children;
          if (Tool.isNotEmpty(children)){
            for (let j =0;j<children.length;j++){
              setDisable(children,children[j].id)
            }
          }
        }else {
          //如果当前节点不是目标节点，则到其子节点再找找看
          const children = node.children;
          if (Tool.isNotEmpty(children)){
            setDisable(children,id);
          }
        }
      }
    };

    const ids:Array<String> =[];
    /**
     * 查找整根树枝
     */
    const getDeleteIds=(treeSelectData:any,id:any)=>{
      //consoLe.Log(treeselectData, id );
      // 逦历数组，即逦历某一层节点
      for (let i =0;i < treeSelectData.length;i++){
        const node = treeSelectData[i];
        if (node.id === id){
          // 如果当前节点就是目标节点
          console.log("delete",node);
          // 将目标id放入结果集ids
          node.disabled = true;
          ids.push(id);

          //遍历所有子节点
          const children = node.children;
          if (Tool.isNotEmpty(children)){
            for (let j =0;j<children.length;j++){
              getDeleteIds(children,children[j].id)
            }
          }
        }else {
          //如果当前节点不是目标节点，则到其子节点再找找看
          const children = node.children;
          if (Tool.isNotEmpty(children)){
            getDeleteIds(children,id);
          }
        }
      }
    };

    /**
     * 内容查询
     **/
    const handleQueryContent = () => {
      axios.get("/doc/find-contend/"+doc.value.id).then((response) => {
        loading.value = false;
        const data = response.data;
        if(data.success){
          editor.txt.html(data.content)
        }else {
          message.error(data.success);
        }
      });
    };


    /**
     * 编辑
     */
    const edit = ( record:any ) =>{
      //清空富文本框
      editor.txt.html("")
      modalVisible .value = true;
      doc.value = Tool.copy(record);
      handleQueryContent();
      //不能选择当前节点及其所有子节点作为父节点，会使树断开
      treeSelectData.value=Tool.copy(level1.value);
      setDisable(treeSelectData.value,record.id);

      //为选择树添加一个“无”
      treeSelectData.value.unshift({id:0,name:'无'});

    };
    /**
     * 添加
     */
    const add = () =>{
      //清空富文本框
      editor.txt.html("")
      modalVisible .value = true;
      doc.value={
        ebookId:route.query.ebookId
      };

      treeSelectData.value=Tool.copy(level1.value) || [];
      //为选择树添加一个“无”
      treeSelectData.value.unshift({id:0,name:'无'});

    };


    /**
     * 删除
     */
    const handleDelete = ( id:number ) =>{
      getDeleteIds(level1.value,id);
      axios.delete("/doc/delete/"+ids.join(",")).then((response)=>{
        const data = response.data;  //commonResp
        if(data.success){
          //重新加载列表
          handleQuery();
        }
      })
    };

    //-----------富文本预览---------------------
    const drawerVisible = ref(false);
    const previewHtml = ref();
    const handlePreviewContent = () =>{
      const html = editor.txt.html();
      previewHtml.value = html;
      drawerVisible.value = true;
    };
    const onDrawerClose = () => {
      drawerVisible.value=false;
    };

    onMounted(() => {
      handleQuery();

      editor.create()

    });

    return {
      param,//增加按名字查询
      //docs,//表格
      level1,
      columns,
      loading,
      handleQuery,//增加按名字查询


      edit,
      add,
      handleDelete,

      doc,
      modalVisible,
      modalLoading,
      handleSave,
      treeSelectData,

      drawerVisible,
      previewHtml,
      handlePreviewContent,
      onDrawerClose,
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


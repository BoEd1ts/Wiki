<template>
  <a-layout>
    <a-layout-content :style="{background:'#fff',padding: '24px', margin: 6,minHeight: '280px'}">
      <h3 v-if="level1.length==0">什么都没有捏！</h3>
      <a-row>
        <a-col :span="6">
          <a-tree
            v-if="level1.length>0"
            :tree-data="level1"
            @select="onSelect"
            :replaceFields="{title:'name',key:'id',value:'id'}"
            :defaultExoandAll="true"
            ：defaultSelectedKeys="defaultSelectedKeys"
          >

          </a-tree>
        </a-col>
        <a-col :span="18">
          <div class="wangeditor" :innerHTML="html"></div>
        </a-col>
      </a-row>
    </a-layout-content>
  </a-layout>
</template>

<script lang="ts">
import {defineComponent, onMounted, ref} from 'vue';//写上onMounted VUE3.0 setup集成了 导入ref 做响应式数据
import axios from 'axios';
import {message} from "ant-design-vue";
import {Tool} from "@/util/tool";
import {useRoute} from "vue-router";
import E from 'wangeditor';

export default defineComponent({
  name: 'doc',
  setup() {
    const  route = useRoute()
    const docs = ref();
    const html =ref();
    const defaultSelectedKeys = ref();
    defaultSelectedKeys.value=[];


    const level1 = ref(); //一级分类树，children就是二级分类
    level1.value=[];


    /**
     * 内容查询
     **/
    const handleQueryContent = (id:number) => {
      axios.get("/doc/find-contend/"+id).then((response) => {
        const data = response.data;
        if(data.success){
          html.value=data.content
        }else {
          message.error(data.success);
        }
      });
    };

    /**
     * 数据查询
     **/
    const handleQuery = () => {

      axios.get("/doc/all/"+ route.query.ebookId).then((response) => {
        const data = response.data;
        if(data.success){
          docs.value = data.content;

          level1.value=[];
          level1.value=Tool.array2Tree(docs.value,0);

          if (Tool.isNotEmpty(level1)){
            defaultSelectedKeys.value=[level1.value[0].id]
            handleQueryContent(level1.value[0].id)
          }
        }else {
          message.error(data.success);
        }
      });
    };



    const onSelect = (selectedKeys:any,info:any) =>{
      console.log('selected',selectedKeys,info);
      if (Tool.isNotEmpty(selectedKeys)){
        //加载内容
        handleQueryContent(selectedKeys[0]);
      }
    }


    onMounted(() => {
      handleQuery();
    });

    return {
      level1,
      html,
      onSelect,
      defaultSelectedKeys
    }
  }
});
</script>

<style>
/* table 样式 */
.wangeditor table {
  border-top: 1px solid #ccc;
  border-left: 1px solid #ccc;
}

.wangeditor table td,
.wangeditor table th {
  border-bottom: 1px solid #ccc;
  border-right: 1px solid #ccc;
  padding: 3px 5px;
}

.wangeditor table th {
  border-bottom: 2px solid #ccc;
  text-align: center;
}

/* blockquote 样式 */
.wangeditor blockquote {
  display: block;
  border-left: 8px solid #d0e5f2;
  padding: 5px 10px;
  margin: 10px 0;
  line-height: 1.4;
  font-size: 100%;
  background-color: #f1f1f1;
}

/* code 样式 */
.wangeditor code {
  display: inline-block;
  *display: inline;
  *zoom: 1;
  background-color: #f1f1f1;
  border-radius: 3px;
  padding: 3px 5px;
  margin: 0 3px;
}

.wangeditor pre code {
  display: block;
}

/* ul ol 样式 */
.wangeditor ul, ol {
  margin: 10px 0 10px 20px;
}

/* 和antdv p冲突，覆盖掉 */
.wangeditor blockquote p {
  font-family: "YouYuan";
  margin: 20px 10px !important;
  font-size: 16px !important;
  font-weight: 600;
}

/* 点赞 */
.vote-div {
  padding: 15px;
  text-align: center;
}

/* 图片自适应 */
.wangeditor img {
  max-width: 100%;
  height: auto;
}

/* 视频自适应 */
.wangeditor iframe {
  width: 100%;
  height: 400px;
}

/*和antdv p 冲突 覆盖掉  !important让这个样式优先级提到最高*/
.wangeditor blockquote p{
  font-family: "YouYuan";
  margin: 20px 10px !important;
  font-size: 16px !important;
  font-weight: 600;
}
</style>
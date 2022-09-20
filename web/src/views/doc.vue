<template>
  <a-layout>
    <a-layout-content :style="{background:'#fff',padding: '24px', margin: 6,minHeight: '280px'}">
      <a-row>
        <a-col :span="6">
          <a-tree
            v-if="level1.length>0"
            :tree-data="level1"
            @select="onselect"
            :replaceFields="{title:'name',key:'id',value:'id'}"
            :defaultExoandAll="true"
          >

          </a-tree>
        </a-col>
        <a-col :span="18">

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

    const level1 = ref(); //一级分类树，children就是二级分类
    level1.value=[];

    /**
     * 数据查询
     **/
    const handleQuery = () => {

      axios.get("/doc/all").then((response) => {
        const data = response.data;
        if(data.success){
          docs.value = data.content;

          level1.value=[];
          level1.value=Tool.array2Tree(docs.value,0);
        }else {
          message.error(data.success);
        }
      });
    };



    onMounted(() => {
      handleQuery();
    });

    return {
      level1,
    }
  }
});
</script>
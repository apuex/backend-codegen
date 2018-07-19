package com.github.apuex.codegen.springboot


object GenerateAll extends App {
  Project.main(args)
  App.main(args)
  Controller.main(args)
  Dao.main(args)
  Message.main(args)
  Service.main(args)
}

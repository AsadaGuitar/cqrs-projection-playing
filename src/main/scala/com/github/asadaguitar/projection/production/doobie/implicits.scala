package com.github.asadaguitar.projection.production.doobie

import doobie.{free, syntax}

object implicits extends free.Instances
  with syntax.AllSyntax
  with doobie.util.meta.SqlMeta
  with doobie.util.meta.TimeMeta
  with doobie.util.meta.LegacyMeta
  with doobie.util.meta.MetaConstructors
  with doobie.util.meta.SqlMetaInstances
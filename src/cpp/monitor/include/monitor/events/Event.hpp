/*
 * Event.hpp
 *
 * Copyright (C) 2009-12 by RStudio, Inc.
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

#ifndef MONITOR_EVENTS_EVENT_HPP
#define MONITOR_EVENTS_EVENT_HPP

#include <boost/date_time/posix_time/posix_time.hpp>

#include <core/system/System.hpp>

namespace monitor {
namespace events {

enum EventScope
{
   kAuthScope = 0,
   kSessionScope = 1
};

#define kAuthLoginEvent       1001
#define kAuthLogoutEvent      1002

#define kSessionStartEvent    2001
#define kSessionSuspendEvent  2002
#define kSessionQuitEvent     2003
#define kSessionExitEvent     2004

class Event
{
public:
   Event(EventScope scope,
         int id,
         const std::string& username,
         PidType pid,
         boost::posix_time::ptime timestamp =
                        boost::posix_time::microsec_clock::universal_time(),
         const std::string& data = std::string())
      : scope_(scope),
        id_(id),
        username_(username),
        pid_(pid),
        timestamp_(timestamp),
        data_(data)
   {
   }

   Event(EventScope scope,
         int id,
         const std::string& data = std::string());

public:
   EventScope scope() const { return scope_; }
   int id() const { return id_; }
   const std::string& username() const { return username_; }
   PidType pid() const { return pid_; }
   boost::posix_time::ptime timestamp() const { return timestamp_; }
   const std::string& data() const { return data_; }

private:
   EventScope scope_;
   int id_;
   std::string username_;
   PidType pid_;
   boost::posix_time::ptime timestamp_;
   std::string data_;
};

} // namespace events
} // namespace monitor

#endif // MONITOR_EVENTS_EVENT_HPP
